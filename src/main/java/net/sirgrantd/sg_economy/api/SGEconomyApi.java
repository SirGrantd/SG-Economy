package net.sirgrantd.sg_economy.api;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.sirgrantd.sg_economy.SGEconomyMod;
import net.sirgrantd.sg_economy.internal.attachments.CurrencyPlayerAttachment;
import net.sirgrantd.sg_economy.internal.client.SyncClientConfig;
import net.sirgrantd.sg_economy.internal.config.ServerConfig;

public final class SGEconomyApi {

    private SGEconomyApi() {
    }

    public static boolean isDecimalSystem() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            return SyncClientConfig.isDecimalCurrency();
        }
        return ServerConfig.isDecimalCurrency;
    }

    public static double getBalance(Entity entity) {
        long internalBalance = entity.getData(SGEconomyMod.CURRENCY_PLAYER).getCount();

        if (isDecimalSystem()) {
            return internalBalance / 100.0;
        }
        return (double) internalBalance;
    }

    public static boolean hasBalance(Entity entity, double amount) {
        return getBalance(entity) >= amount;
    }

    public static boolean setBalance(Entity entity, double amount) {
        if (amount < 0)
            return false;

        if (entity instanceof ServerPlayer player) {
            long internalAmount = isDecimalSystem() ? Math.round(amount * 100.0) : Math.round(amount);

            CurrencyPlayerAttachment data = player.getData(SGEconomyMod.CURRENCY_PLAYER);
            data.setBalance(internalAmount);
            player.setData(SGEconomyMod.CURRENCY_PLAYER, data);

            data.syncToClient(player);
            return true;
        }
        return false;
    }

    public static boolean depositBalance(Entity entity, double amount) {
        if (amount < 0)
            return false;

        if (entity instanceof ServerPlayer player) {
            long internalAmount = isDecimalSystem() ? Math.round(amount * 100.0) : Math.round(amount);

            CurrencyPlayerAttachment data = player.getData(SGEconomyMod.CURRENCY_PLAYER);
            data.addBalance(internalAmount);
            player.setData(SGEconomyMod.CURRENCY_PLAYER, data);

            data.syncToClient(player);
            return true;
        }
        return false;
    }

    public static boolean withdrawBalance(Entity entity, double amount) {
        if (amount < 0)
            return false;

        if (entity instanceof ServerPlayer player) {
            if (!hasBalance(player, amount)) {
                return false;
            }

            long internalAmount = isDecimalSystem() ? Math.round(amount * 100.0) : Math.round(amount);

            CurrencyPlayerAttachment data = player.getData(SGEconomyMod.CURRENCY_PLAYER);
            data.removeBalance(internalAmount);
            player.setData(SGEconomyMod.CURRENCY_PLAYER, data);

            data.syncToClient(player);
            return true;
        }
        return false;
    }

    public static boolean transferBalance(Entity from, Entity to, double amount) {
        if (withdrawBalance(from, amount)) {
            depositBalance(to, amount);
            return true;
        }
        return false;
    }

}