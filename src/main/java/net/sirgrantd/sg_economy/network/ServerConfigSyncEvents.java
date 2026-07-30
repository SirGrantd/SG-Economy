package net.sirgrantd.sg_economy.network;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.sirgrantd.sg_economy.SGEconomyMod;
import net.sirgrantd.sg_economy.config.ServerConfig;
import net.sirgrantd.sg_economy.network.payload.SyncServerConfigS2C;

@EventBusSubscriber(modid = SGEconomyMod.MOD_ID)
public final class ServerConfigSyncEvents {

    private ServerConfigSyncEvents() {}

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        PacketDistributor.sendToPlayer(
                player,
                new SyncServerConfigS2C(
                        ServerConfig.isDecimalCurrency,
                        ServerConfig.percentageCoinsSaveOnDeath,
                        ServerConfig.isActivePayCommand
                )
        );
    }
}