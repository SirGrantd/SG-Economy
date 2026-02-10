package net.sirgrantd.sg_economy.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class ClientSyncedConfig {
    private static boolean decimalCurrency = true;
    private static int percentageCoinsSaveOnDeath = 100;
    private static boolean isActivePayCommand = true;

    private ClientSyncedConfig() {}

    public static boolean isDecimalCurrency() {
        return decimalCurrency;
    }

    public static int getPercentageCoinsSaveOnDeath() {
        return percentageCoinsSaveOnDeath;
    }

    public static boolean isActivePayCommand() {
        return isActivePayCommand;
    }

    public static void apply(boolean decimalCurrency, int percentageCoinsSaveOnDeath, boolean isActivePayCommand) {
        ClientSyncedConfig.decimalCurrency = decimalCurrency;
        ClientSyncedConfig.percentageCoinsSaveOnDeath = percentageCoinsSaveOnDeath;
        ClientSyncedConfig.isActivePayCommand = isActivePayCommand;
    }
}