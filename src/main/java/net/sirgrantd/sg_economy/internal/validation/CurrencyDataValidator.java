package net.sirgrantd.sg_economy.internal.validation;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.sirgrantd.sg_economy.SGEconomyMod;
import net.sirgrantd.sg_economy.api.validation.ICurrencyDataValidator;
import net.sirgrantd.sg_economy.internal.attachments.CurrencyPlayerAttachment;
import net.sirgrantd.sg_economy.internal.attachments.LegacyCoinsBagAttachment;

public class CurrencyDataValidator implements ICurrencyDataValidator {

    private static final CurrencyDataValidator INSTANCE = new CurrencyDataValidator();

    private static final String[] LEGACY_TAG_KEYS = {
            "magic_coins:coins_in_bag",
            "sg_economy:coins_in_bag",
            "magic_coins",
            "ValueTotalInCurrency",
            "ValueTotalInCoins"
    };

    private CurrencyDataValidator() {
    }

    public static CurrencyDataValidator getInstance() {
        return INSTANCE;
    }

    @Override
    public long sanitize(long amount) {
        if (amount < 0) {
            return 0L;
        }
        return amount;
    }

    @Override
    public double sanitize(double amount) {
        if (Double.isNaN(amount) || Double.isInfinite(amount) || amount < 0.0) {
            return 0.0;
        }
        return Math.round(amount * 100.0) / 100.0;
    }

    @Override
    public long parseAndMigrateNbt(CompoundTag nbt) {
        if (nbt == null || nbt.isEmpty()) {
            return 0L;
        }

        // 1. Format SG-Economy ("balance")
        if (nbt.contains("balance", Tag.TAG_LONG)) {
            return sanitize(nbt.getLong("balance"));
        } else if (nbt.contains("balance", Tag.TAG_INT)) {
            return sanitize((long) nbt.getInt("balance"));
        } else if (nbt.contains("balance", Tag.TAG_DOUBLE)) {
            return sanitize(Math.round(nbt.getDouble("balance") * 100.0));
        }

        // 2. Format Legacy Decimal ("ValueTotalInCurrency")
        if (nbt.contains("ValueTotalInCurrency", Tag.TAG_LONG)) {
            long val = nbt.getLong("ValueTotalInCurrency");
            return sanitize(val);
        } else if (nbt.contains("ValueTotalInCurrency", Tag.TAG_DOUBLE)) {
            double val = nbt.getDouble("ValueTotalInCurrency");
            return sanitize(Math.round(val * 100.0));
        } else if (nbt.contains("ValueTotalInCurrency", Tag.TAG_INT)) {
            return sanitize((long) nbt.getInt("ValueTotalInCurrency"));
        }

        // 3. Format Legacy Integer ("ValueTotalInCoins")
        if (nbt.contains("ValueTotalInCoins", Tag.TAG_INT)) {
            return sanitize((long) nbt.getInt("ValueTotalInCoins"));
        } else if (nbt.contains("ValueTotalInCoins", Tag.TAG_LONG)) {
            return sanitize(nbt.getLong("ValueTotalInCoins"));
        }

        // 4. Format Legacy Integer ("coins" or "currency")
        if (nbt.contains("coins", Tag.TAG_INT)) {
            return sanitize((long) nbt.getInt("coins"));
        } else if (nbt.contains("currency", Tag.TAG_LONG)) {
            return sanitize(nbt.getLong("currency"));
        }

        return 0L;
    }

    @Override
    public boolean migrateAndCleanPlayer(ServerPlayer player) {
        if (player == null) {
            return false;
        }

        boolean modified = false;
        long migratedBalance = 0L;

        // 1. Check for legacy NeoForge Data Attachments (magic_coins:coins_in_bag & sg_economy:coins_in_bag)
        if (player.hasData(SGEconomyMod.LEGACY_MAGIC_COINS_BAG)) {
            LegacyCoinsBagAttachment legacy = player.getData(SGEconomyMod.LEGACY_MAGIC_COINS_BAG);
            if (legacy != null) {
                long found = legacy.getMigratedBalance();
                if (found > migratedBalance) {
                    migratedBalance = found;
                }
            }
            player.removeData(SGEconomyMod.LEGACY_MAGIC_COINS_BAG);
            modified = true;
        }

        if (player.hasData(SGEconomyMod.LEGACY_SG_COINS_BAG)) {
            LegacyCoinsBagAttachment legacy = player.getData(SGEconomyMod.LEGACY_SG_COINS_BAG);
            if (legacy != null) {
                long found = legacy.getMigratedBalance();
                if (found > migratedBalance) {
                    migratedBalance = found;
                }
            }
            player.removeData(SGEconomyMod.LEGACY_SG_COINS_BAG);
            modified = true;
        }

        CompoundTag persistentData = player.getPersistentData();

        // 2. Check for legacy tags directly in the player's persistent data
        for (String key : LEGACY_TAG_KEYS) {
            if (persistentData.contains(key)) {
                if (persistentData.contains(key, Tag.TAG_COMPOUND)) {
                    CompoundTag legacyCompound = persistentData.getCompound(key);
                    long found = parseAndMigrateNbt(legacyCompound);
                    if (found > migratedBalance) {
                        migratedBalance = found;
                    }
                } else {
                    long found = parseAndMigrateNbt(persistentData);
                    if (found > migratedBalance) {
                        migratedBalance = found;
                    }
                }
                persistentData.remove(key);
                modified = true;
            }
        }

        // 2. Check for legacy tags inside the "persisted" compound tag
        if (persistentData.contains(ServerPlayer.PERSISTED_NBT_TAG, Tag.TAG_COMPOUND)) {
            CompoundTag persistedCompound = persistentData.getCompound(ServerPlayer.PERSISTED_NBT_TAG);
            for (String key : LEGACY_TAG_KEYS) {
                if (persistedCompound.contains(key)) {
                    if (persistedCompound.contains(key, Tag.TAG_COMPOUND)) {
                        CompoundTag legacyCompound = persistedCompound.getCompound(key);
                        long found = parseAndMigrateNbt(legacyCompound);
                        if (found > migratedBalance) {
                            migratedBalance = found;
                        }
                    }
                    persistedCompound.remove(key);
                    modified = true;
                }
            }
        }

        // If we found a migrated balance, we should set it in the player's attachment
        // if it's not already set
        // We only set it if the player's current balance is 0, to avoid overwriting
        // existing balances.
        if (migratedBalance > 0L) {
            CurrencyPlayerAttachment attachment = player.getData(SGEconomyMod.CURRENCY_PLAYER);
            if (attachment.getCount() == 0L) {
                attachment.setBalance(migratedBalance);
                player.setData(SGEconomyMod.CURRENCY_PLAYER, attachment);
                attachment.syncToClient(player);
                SGEconomyMod.LOGGER.info("[SG-Economy] currency balance migrated for player {} ({}): {}",
                        player.getName().getString(), player.getStringUUID(), migratedBalance);
            }
        }

        return modified;
    }
}
