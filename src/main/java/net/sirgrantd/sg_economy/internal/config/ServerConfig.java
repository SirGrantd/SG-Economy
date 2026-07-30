package net.sirgrantd.sg_economy.internal.config;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.network.PacketDistributor;
import net.sirgrantd.sg_economy.SGEconomyMod;
import net.sirgrantd.sg_economy.internal.network.payloads.SyncServerConfigS2C;

@EventBusSubscriber(modid = SGEconomyMod.MOD_ID)
public class ServerConfig {

    public static boolean isDecimalCurrency;
    public static boolean isEnablePayCommand;

    public static class Config {
        public static final ModConfigSpec.Builder CONFIG_BUILDER = new ModConfigSpec.Builder();

        public static final ModConfigSpec.ConfigValue<Boolean> IS_DECIMAL_CURRENCY;
        public static final ModConfigSpec.ConfigValue<Boolean> IS_ENABLE_PAY_COMMAND;

        public static final ModConfigSpec SPEC;

        static {
            CONFIG_BUILDER.push("ECONOMY SETTINGS");

            IS_DECIMAL_CURRENCY = CONFIG_BUILDER
                    .comment(
                            "If true, the economy will use decimal currency (e.g., 10.5 coins). If false, it will use whole numbers only.")
                    .comment("Note: Changing this setting affects players existing balances.")
                    .define("is_decimal_currency", false);

            CONFIG_BUILDER.pop();

            CONFIG_BUILDER.push("PAY COMMAND");

            IS_ENABLE_PAY_COMMAND = CONFIG_BUILDER
                    .comment("If true, the /coins pay command will be active.")
                    .define("is_enable_pay_command", true);

            CONFIG_BUILDER.pop();

            SPEC = CONFIG_BUILDER.build();
        }
    }

    public static void bakeConfig() {
        isDecimalCurrency = Config.IS_DECIMAL_CURRENCY.get();
        isEnablePayCommand = Config.IS_ENABLE_PAY_COMMAND.get();

        PacketDistributor.sendToAllPlayers(
                new SyncServerConfigS2C(isDecimalCurrency, isEnablePayCommand));
    }

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent.Loading event) {
        if (event.getConfig().getType() == ModConfig.Type.SERVER
                && event.getConfig().getSpec() == Config.SPEC) {
            bakeConfig();
        }
    }

    @SubscribeEvent
    public static void onReload(final ModConfigEvent.Reloading event) {
        if (event.getConfig().getType() == ModConfig.Type.SERVER
                && event.getConfig().getSpec() == Config.SPEC) {

            bakeConfig();
        }
    }
}
