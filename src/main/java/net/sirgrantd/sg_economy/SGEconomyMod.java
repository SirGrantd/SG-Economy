package net.sirgrantd.sg_economy;

import java.util.function.Supplier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.sirgrantd.celesthyd.api.CelesthydApi;
import net.sirgrantd.sg_economy.internal.attachments.CurrencyPlayerAttachment;
import net.sirgrantd.sg_economy.internal.config.ClientConfig;
import net.sirgrantd.sg_economy.internal.config.ServerConfig;
import net.sirgrantd.sg_economy.internal.network.payloads.SyncServerConfigS2C;

@Mod(SGEconomyMod.MOD_ID)
public class SGEconomyMod {
        public static final Logger LOGGER = LogManager.getLogger(SGEconomyMod.class);
        public static final String MOD_ID = "sg_economy";

        public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister
                        .create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, MOD_ID);

        public static final Supplier<AttachmentType<CurrencyPlayerAttachment>> CURRENCY_PLAYER = ATTACHMENT_TYPES
                        .register("currency_player", () -> AttachmentType
                                        .serializable(() -> new CurrencyPlayerAttachment()).copyOnDeath().build());

        public SGEconomyMod(IEventBus eventBus, ModContainer modContainer) {
                ATTACHMENT_TYPES.register(eventBus);

                modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfig.Config.SPEC,
                                String.format("%s-server.toml", MOD_ID));
                modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.Config.SPEC,
                                String.format("%s-client.toml", MOD_ID));
        }

        @SubscribeEvent
        public static void onCommonSetupEvent(FMLCommonSetupEvent event) {
                event.enqueueWork(() -> {
                        CelesthydApi.registerConfigSync(() -> new SyncServerConfigS2C(
                                        ServerConfig.isDecimalCurrency,
                                        ServerConfig.isEnablePayCommand));

                        CelesthydApi.registerAutoSyncAttachment(CURRENCY_PLAYER);
                });
        }
}