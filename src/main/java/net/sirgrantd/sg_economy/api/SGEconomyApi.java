package net.sirgrantd.sg_economy.api;

import net.sirgrantd.sg_economy.api.event.EconomyEventProvider;
import net.sirgrantd.sg_economy.api.economy.EconomyProvider;
import net.sirgrantd.sg_economy.internal.DeathEventEconomyProvider;
import net.sirgrantd.sg_economy.internal.DefaultEconomyProvider;

public class SGEconomyApi {

    private static final EconomyProvider SgEconomy = DefaultEconomyProvider.INSTANCE;
    private static final EconomyEventProvider SgEconomyEvents = DeathEventEconomyProvider.INSTANCE;

    public static EconomyProvider getSGEconomy() {
        return SgEconomy;
    }

    public static EconomyEventProvider getSGEconomyEvents() {
        return SgEconomyEvents;
    }

}
