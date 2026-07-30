package net.sirgrantd.sg_economy.internal.client;

public final class SyncClientConfig {
    private static boolean isDecimalCurrency;
    private static boolean isEnablePayCommand;

    private SyncClientConfig() {
    }

    public static boolean isDecimalCurrency() {
        return isDecimalCurrency;
    }

    public static boolean isEnablePayCommand() {
        return isEnablePayCommand;
    }

    public static void apply(boolean isDecimalCurrency, boolean isEnablePayCommand) {
        SyncClientConfig.isDecimalCurrency = isDecimalCurrency;
        SyncClientConfig.isEnablePayCommand = isEnablePayCommand;
    }
}
