package net.sirgrantd.sg_economy.config;

import java.util.function.Supplier;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import net.sirgrantd.sg_economy.SGEconomyMod;

@EventBusSubscriber(modid = SGEconomyMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientConfig {

    public static int xDisplayCurrency;
    public static int yDisplayCurrency;
    public static boolean activeDisplayCurrency = true;
    public static String typeDisplayCurrency = "default";

    public static class Config {
        public static final Supplier<Integer> X_DISPLAY_CURRENCY;
        public static final Supplier<Integer> Y_DISPLAY_CURRENCY;
        public static final Supplier<Boolean> ACTIVE_DISPLAY_CURRENCY;
        public static final Supplier<String> TYPE_DISPLAY_CURRENCY;

        static {
            ModConfigSpec.Builder CONFIG_BUILDER = new ModConfigSpec.Builder();

            CONFIG_BUILDER.push("DISPLAYS");

            X_DISPLAY_CURRENCY = CONFIG_BUILDER
                    .comment("The x position of the display for coins")
                    .comment("Tips: 80 for align for right side")
                    .defineInRange("xDisplayCurrency", 0, Integer.MIN_VALUE, Integer.MAX_VALUE);

            Y_DISPLAY_CURRENCY = CONFIG_BUILDER
                    .comment("The y position of the display for coins")
                    .defineInRange("yDisplayCurrency", 0, Integer.MIN_VALUE, Integer.MAX_VALUE);

            ACTIVE_DISPLAY_CURRENCY = CONFIG_BUILDER
                    .comment("Whether to display the coin amount in the inventory screen")
                    .define("activeDisplayCurrency", true);

            TYPE_DISPLAY_CURRENCY = CONFIG_BUILDER
                    .comment("Type of currency display: \"default\" or \"magic_coins\"")
                    .define("typeDisplayCurrency", "default");

            CONFIG_BUILDER.pop();

            SPEC = CONFIG_BUILDER.build();
        }

        public static final ModConfigSpec SPEC;
    }

    private static void bakeConfig() {
        xDisplayCurrency = Config.X_DISPLAY_CURRENCY.get();
        yDisplayCurrency = Config.Y_DISPLAY_CURRENCY.get();
        activeDisplayCurrency = Config.ACTIVE_DISPLAY_CURRENCY.get();
        typeDisplayCurrency = Config.TYPE_DISPLAY_CURRENCY.get();
    }

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent event) {
        if (event.getConfig().getType() == ModConfig.Type.CLIENT && event.getConfig().getSpec() == Config.SPEC) {
            bakeConfig();
        }
    }

}