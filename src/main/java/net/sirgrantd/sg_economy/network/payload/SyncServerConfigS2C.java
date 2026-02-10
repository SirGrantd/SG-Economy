package net.sirgrantd.sg_economy.network.payload;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import net.sirgrantd.sg_economy.SGEconomyMod;
import net.sirgrantd.sg_economy.client.ClientSyncedConfig;

public record SyncServerConfigS2C(
        boolean decimalCurrency,
        int percentageCoinsSaveOnDeath,
        boolean isActivePayCommand
) implements CustomPacketPayload {

    public static final Type<SyncServerConfigS2C> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SGEconomyMod.MOD_ID, "sync_server_config"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncServerConfigS2C> STREAM_CODEC =
            StreamCodec.of(
                    (buf, msg) -> {
                        buf.writeBoolean(msg.decimalCurrency);
                        buf.writeInt(msg.percentageCoinsSaveOnDeath);
                        buf.writeBoolean(msg.isActivePayCommand);
                    },
                    (buf) -> new SyncServerConfigS2C(
                            buf.readBoolean(),
                            buf.readInt(),
                            buf.readBoolean()
                    )
            );

    @Override
    public Type<SyncServerConfigS2C> type() {
        return TYPE;
    }

    public static void handle(final SyncServerConfigS2C msg, final IPayloadContext ctx) {
        if (ctx.flow() != PacketFlow.CLIENTBOUND) {
            return;
        }

        ctx.enqueueWork(() -> applyClient(msg));
    }

    @OnlyIn(Dist.CLIENT)
    private static void applyClient(SyncServerConfigS2C msg) {
        ClientSyncedConfig.apply(
                msg.decimalCurrency(),
                msg.percentageCoinsSaveOnDeath(),
                msg.isActivePayCommand()
        );
    }
}