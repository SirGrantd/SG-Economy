package net.sirgrantd.sg_economy.internal;

import net.minecraft.world.entity.Entity;
import net.sirgrantd.sg_economy.api.event.SGEventProvider;
import net.sirgrantd.sg_economy.capabilities.CoinsBagCapabilities;
import net.sirgrantd.sg_economy.capabilities.CoinsBagCapabilities.CoinsInBag;
import net.sirgrantd.sg_economy.config.ServerConfig;

public enum EventEconomyProvider implements SGEventProvider {
    INSTANCE;
    
    public boolean isCoinsLostOnDeath(Entity entity) {
        return entity.getData(CoinsBagCapabilities.COINS_IN_BAG).isCoinsLostOnDeath;
    }

    public void setCoinsLostOnDeath(Entity entity, boolean value) {
        CoinsInBag coinsInBag = entity.getData(CoinsBagCapabilities.COINS_IN_BAG);
        coinsInBag.isCoinsLostOnDeath = value;
        coinsInBag.syncCoinsInBag(entity);
    }

    public int getPercentageCoinsSaveOnDeath() {
        return ServerConfig.percentageCoinsSaveOnDeath;
    }
}
