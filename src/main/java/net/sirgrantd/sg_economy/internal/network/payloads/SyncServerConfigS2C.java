package net.sirgrantd.sg_economy.internal.network.payloads;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.sirgrantd.sg_economy.SGEconomyMod;

public record SyncServerConfigS2C(
        boolean isDecimalCurrency,
        boolean isEnablePayCommand) implements CustomPacketPayload {

    public static final Type<SyncServerConfigS2C> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(SGEconomyMod.MOD_ID, "sync_server_config"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncServerConfigS2C> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, SyncServerConfigS2C::isDecimalCurrency,
            ByteBufCodecs.BOOL, SyncServerConfigS2C::isEnablePayCommand,
            SyncServerConfigS2C::new);

    @Override
    public Type<SyncServerConfigS2C> type() {
        return TYPE;
    }
}
