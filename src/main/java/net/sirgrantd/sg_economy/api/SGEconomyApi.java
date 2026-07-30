package net.sirgrantd.sg_economy.api;

import net.sirgrantd.sg_economy.internal.EconomyEventServiceImpl;

public class SGEconomyApi {

    private static final EconomyEventProvider INSTANCE = new EconomyEventServiceImpl();

    private SGEconomyApi() {}

    public static EconomyEventProvider get() {
        return INSTANCE;
    }

}
