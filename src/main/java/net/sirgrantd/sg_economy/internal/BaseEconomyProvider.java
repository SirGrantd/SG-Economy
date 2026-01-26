package net.sirgrantd.sg_economy.internal;

import net.minecraft.world.entity.Entity;
import net.sirgrantd.sg_economy.api.economy.SGEconomyProvider;
import net.sirgrantd.sg_economy.capabilities.CoinsBagCapabilities;
import net.sirgrantd.sg_economy.capabilities.CoinsBagCapabilities.CoinsInBag;
import net.sirgrantd.sg_economy.config.ServerConfig;

public enum BaseEconomyProvider implements SGEconomyProvider {
    INSTANCE;

    @Override
    public boolean hasCapability(Entity entity) {
        return entity.getData(CoinsBagCapabilities.COINS_IN_BAG) != null;
    }

    @Override
    public double getCurrency(Entity entity) {
        if (ServerConfig.isDecimalCurrency) {
            return entity.getData(CoinsBagCapabilities.COINS_IN_BAG).valueTotalInCurrency;
        }       
        return (double) entity.getData(CoinsBagCapabilities.COINS_IN_BAG).valueTotalInCoins;
    }

    @Override
    public void setCurrency(Entity entity, double amount) {
        CoinsInBag coinsInBag = entity.getData(CoinsBagCapabilities.COINS_IN_BAG);
        if (ServerConfig.isDecimalCurrency) {
            coinsInBag.valueTotalInCurrency = Math.max(0.0, amount);
        } else {
            coinsInBag.valueTotalInCoins = (int) Math.max(0, Math.floor(amount));
        }
        coinsInBag.syncCoinsInBag(entity);
    }

    @Override
    public void addCurrency(Entity entity, double amount) {
        CoinsInBag coinsInBag = entity.getData(CoinsBagCapabilities.COINS_IN_BAG);
        if (ServerConfig.isDecimalCurrency) {
            coinsInBag.valueTotalInCurrency = Math.max(0.0, getCurrency(entity) + amount);
        } else {
            coinsInBag.valueTotalInCoins = Math.max(0, getCoins(entity) + (int) amount);
        }
        coinsInBag.syncCoinsInBag(entity);
    }

    @Override
    public void removeCurrency(Entity entity, double amount) {
        CoinsInBag coinsInBag = entity.getData(CoinsBagCapabilities.COINS_IN_BAG);
        if (ServerConfig.isDecimalCurrency) {
            coinsInBag.valueTotalInCurrency = Math.max(0.0, getCurrency(entity) - amount);
        } else {
            coinsInBag.valueTotalInCoins = Math.max(0, getCoins(entity) - (int) amount);
        }
        coinsInBag.syncCoinsInBag(entity);
    }

}
