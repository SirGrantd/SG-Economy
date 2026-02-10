package net.sirgrantd.sg_economy.internal;

import net.sirgrantd.sg_economy.api.EconomyEventProvider;

public final class EconomyServices {

    private static final EconomyEventProvider INSTANCE = new EconomyEventServiceImpl();

    private EconomyServices() {}

    public static EconomyEventProvider get() {
        return INSTANCE;
    }
}
