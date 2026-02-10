package net.sirgrantd.sg_economy.network;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.sirgrantd.sg_economy.SGEconomyMod;

public final class SGNetwork {

    private SGNetwork() {}

    private static boolean networkingRegistered = false;

    private static final Map<CustomPacketPayload.Type<?>, NetworkMessage<?>> MESSAGES = new HashMap<>();

    private record NetworkMessage<T extends CustomPacketPayload>(
            StreamCodec<? extends FriendlyByteBuf, T> reader,
            IPayloadHandler<T> handler
    ) {}

    public static <T extends CustomPacketPayload> void addNetworkMessage(
            CustomPacketPayload.Type<T> id,
            StreamCodec<? extends FriendlyByteBuf, T> reader,
            IPayloadHandler<T> handler
    ) {
        if (networkingRegistered) {
            throw new IllegalStateException("Cannot register new network messages after networking has been registered");
        }
        MESSAGES.put(id, new NetworkMessage<>(reader, handler));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static void registerNetworking(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(SGEconomyMod.MOD_ID);
        MESSAGES.forEach((id, networkMessage) ->
                registrar.playBidirectional(
                        id,
                        ((NetworkMessage) networkMessage).reader(),
                        ((NetworkMessage) networkMessage).handler()
                )
        );
        networkingRegistered = true;
    }
}