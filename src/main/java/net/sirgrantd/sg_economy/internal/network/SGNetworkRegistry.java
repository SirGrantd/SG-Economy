package net.sirgrantd.sg_economy.internal.network;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.sirgrantd.celesthyd.internal.network.CelesthydPayloadHandler;
import net.sirgrantd.sg_economy.SGEconomyMod;
import net.sirgrantd.sg_economy.internal.attachments.CurrencyPlayerAttachment;
import net.sirgrantd.sg_economy.internal.client.SyncClientConfig;
import net.sirgrantd.sg_economy.internal.network.payloads.*;

@EventBusSubscriber(modid = SGEconomyMod.MOD_ID)
public class SGNetworkRegistry {

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(SGEconomyMod.MOD_ID);

        registrar.playToClient(
                SyncServerConfigS2C.TYPE,
                SyncServerConfigS2C.STREAM_CODEC,
                (payload, context) -> CelesthydPayloadHandler.handleClientBound(payload, context, (p, ctx) -> {
                    SyncClientConfig.apply(p.isDecimalCurrency(), p.isEnablePayCommand());
                }));

        registrar.playToClient(
                SyncCurrencyPlayerPayload.TYPE,
                SyncCurrencyPlayerPayload.STREAM_CODEC,
                (payload, context) -> CelesthydPayloadHandler.handleClientBound(payload, context, (p, ctx) -> {

                    Player player = ctx.player();

                    if (player != null) {
                        player.setData(SGEconomyMod.CURRENCY_PLAYER, new CurrencyPlayerAttachment(p.balance()));
                    }
                }));
    }
}