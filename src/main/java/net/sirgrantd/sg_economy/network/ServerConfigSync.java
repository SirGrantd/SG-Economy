package net.sirgrantd.sg_economy.network;

import net.neoforged.neoforge.network.PacketDistributor;
import net.sirgrantd.sg_economy.SGEconomyMod;
import net.sirgrantd.sg_economy.config.ServerConfig;
import net.sirgrantd.sg_economy.network.payload.SyncServerConfigS2C;

public final class ServerConfigSync {
    private ServerConfigSync() {}

    public static void syncToAllPlayersNextTick() {
        
        SGEconomyMod.queueServerWork(1, () -> {
            SGEconomyMod.LOGGER.info("Syncing ServerConfig to all players: decimalCurrency={}", ServerConfig.isDecimalCurrency);

            PacketDistributor.sendToAllPlayers(new SyncServerConfigS2C(
                ServerConfig.isDecimalCurrency,
                ServerConfig.percentageCoinsSaveOnDeath,
                ServerConfig.isActivePayCommand
            ));
        });
    }
}