package net.sirgrantd.sg_economy.internal.network.payloads;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.sirgrantd.sg_economy.SGEconomyMod;

public record SyncCurrencyPlayerPayload(long balance) implements CustomPacketPayload {
    public static final Type<SyncCurrencyPlayerPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(SGEconomyMod.MOD_ID, "sync_currency_player"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncCurrencyPlayerPayload> STREAM_CODEC = StreamCodec
            .composite(
                    ByteBufCodecs.VAR_LONG, SyncCurrencyPlayerPayload::balance,
                    SyncCurrencyPlayerPayload::new);

    @Override
    public Type<SyncCurrencyPlayerPayload> type() {
        return TYPE;
    }
}
