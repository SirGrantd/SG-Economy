package net.sirgrantd.sg_economy.internal.attachments;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.sirgrantd.sg_economy.internal.validation.CurrencyDataValidator;

public class LegacyCoinsBagAttachment implements INBTSerializable<CompoundTag> {

    private int valueTotalInCoins = 0;
    private long valueTotalInCurrency = 0L;
    private boolean isCoinsLostOnDeath = true;

    public LegacyCoinsBagAttachment() {
    }

    public int getValueTotalInCoins() {
        return this.valueTotalInCoins;
    }

    public long getValueTotalInCurrency() {
        return this.valueTotalInCurrency;
    }

    public boolean isCoinsLostOnDeath() {
        return this.isCoinsLostOnDeath;
    }

    public long getMigratedBalance() {
        if (this.valueTotalInCurrency > 0L) {
            return CurrencyDataValidator.getInstance().sanitize(this.valueTotalInCurrency);
        } else if (this.valueTotalInCoins > 0) {
            return CurrencyDataValidator.getInstance().sanitize((long) this.valueTotalInCoins);
        }
        return 0L;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider lookupProvider) {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("ValueTotalInCoins", this.valueTotalInCoins);
        nbt.putLong("ValueTotalInCurrency", this.valueTotalInCurrency);
        nbt.putBoolean("IsCoinsLostOnDeath", this.isCoinsLostOnDeath);
        return nbt;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider lookupProvider, CompoundTag nbt) {
        if (nbt == null) {
            return;
        }

        // 1. Read ValueTotalInCoins
        if (nbt.contains("ValueTotalInCoins", Tag.TAG_INT)) {
            this.valueTotalInCoins = nbt.getInt("ValueTotalInCoins");
        } else if (nbt.contains("ValueTotalInCoins", Tag.TAG_LONG)) {
            this.valueTotalInCoins = (int) nbt.getLong("ValueTotalInCoins");
        }

        // 2. Read ValueTotalInCurrency
        if (nbt.contains("ValueTotalInCurrency", Tag.TAG_LONG)) {
            this.valueTotalInCurrency = nbt.getLong("ValueTotalInCurrency");
        } else if (nbt.contains("ValueTotalInCurrency", Tag.TAG_DOUBLE)) {
            double legacyDouble = nbt.getDouble("ValueTotalInCurrency");
            this.valueTotalInCurrency = Math.round(legacyDouble * 100.0);
        } else if (nbt.contains("ValueTotalInCurrency", Tag.TAG_INT)) {
            this.valueTotalInCurrency = (long) nbt.getInt("ValueTotalInCurrency");
        }

        // 3. Fallback to parseAndMigrateNbt if values are still 0 but other keys exist (e.g. balance, coins, currency)
        if (this.valueTotalInCurrency == 0L && this.valueTotalInCoins == 0) {
            long parsed = CurrencyDataValidator.getInstance().parseAndMigrateNbt(nbt);
            if (parsed > 0L) {
                this.valueTotalInCurrency = parsed;
            }
        }

        // 4. Read IsCoinsLostOnDeath
        if (nbt.contains("IsCoinsLostOnDeath", Tag.TAG_BYTE)) {
            this.isCoinsLostOnDeath = nbt.getBoolean("IsCoinsLostOnDeath");
        }
    }
}
