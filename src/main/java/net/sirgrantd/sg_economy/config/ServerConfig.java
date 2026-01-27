package net.sirgrantd.sg_economy.config;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.sirgrantd.sg_economy.SGEconomyMod;

@EventBusSubscriber(modid = SGEconomyMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ServerConfig {

    public static boolean isDecimalCurrency;
    public static int percentageCoinsSaveOnDeath;

    public static class Config {
        public static final ModConfigSpec.Builder CONFIG_BUILDER = new ModConfigSpec.Builder();
        public static final ModConfigSpec.ConfigValue<Boolean> DECIMAL_CURRENCY;
        public static final ModConfigSpec.ConfigValue<Integer> PERCENTAGE_COINS_SAVE_ON_DEATH;
        
        static {
            CONFIG_BUILDER.push("ECONOMY SETTINGS");
            
            DECIMAL_CURRENCY = CONFIG_BUILDER
                .comment("If true, the economy will use decimal currency (e.g., 10.5 coins). If false, it will use whole numbers only.")
                .comment("Note: Changing this setting affects players existing balances.")
                .define("decimalCurrency", true);
                
            CONFIG_BUILDER.pop();
            
            CONFIG_BUILDER.push("DEATH SETTINGS");

            PERCENTAGE_COINS_SAVE_ON_DEATH = CONFIG_BUILDER
                .comment("The percentage of coins save on death")
                .defineInRange("percentageCoinsSaveOnDeath", 100, 0, 100);
            
            CONFIG_BUILDER.pop();

            SPEC = CONFIG_BUILDER.build();
        }

        public static final ModConfigSpec SPEC;
    }

    public static void bakeConfig() {
        isDecimalCurrency = Config.DECIMAL_CURRENCY.get();
        percentageCoinsSaveOnDeath = Config.PERCENTAGE_COINS_SAVE_ON_DEATH.get();
    }

    private static void onConfigUnload() {
        isDecimalCurrency = true;
        percentageCoinsSaveOnDeath = 100;
    }

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent event) {
        try {
            if (event.getConfig().getType() == ModConfig.Type.SERVER && event.getConfig().getSpec() == Config.SPEC) {
                bakeConfig();
            }
        } catch (Exception e) {
            onConfigUnload();
        }
    }

}
