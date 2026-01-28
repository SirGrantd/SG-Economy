package net.sirgrantd.sg_economy.internal;

import net.minecraft.world.entity.Entity;
import net.sirgrantd.sg_economy.api.economy.EconomyProvider;
import net.sirgrantd.sg_economy.capabilities.CoinsBagCapabilities;
import net.sirgrantd.sg_economy.capabilities.CoinsBagCapabilities.CoinsInBag;
import net.sirgrantd.sg_economy.config.ServerConfig;

public enum DefaultEconomyProvider implements EconomyProvider {
    INSTANCE;

    @Override
    public boolean isDecimalCurrency() {
        return ServerConfig.isDecimalCurrency;
    }

    @Override
    public boolean hasCapability(Entity entity) {
        return entity.getData(CoinsBagCapabilities.COINS_IN_BAG) != null;
    }

    @Override
    public double getCurrency(Entity entity) {
        if (isDecimalCurrency()) {
            return entity.getData(CoinsBagCapabilities.COINS_IN_BAG).valueTotalInCurrency / 100.0;
        }
        return (double) entity.getData(CoinsBagCapabilities.COINS_IN_BAG).valueTotalInCoins;
    }

    @Override
    public void setCurrency(Entity entity, double amount) {
        CoinsInBag coinsInBag = entity.getData(CoinsBagCapabilities.COINS_IN_BAG);
        if (isDecimalCurrency()) {
            coinsInBag.valueTotalInCurrency = Math.max(0L, Math.round(amount * 100.0));
        } else {
            coinsInBag.valueTotalInCoins = (int) Math.max(0, Math.floor(amount));
        }
        coinsInBag.syncCoinsInBag(entity);
    }

    @Override
    public void addCurrency(Entity entity, double amount) {
        CoinsInBag coinsInBag = entity.getData(CoinsBagCapabilities.COINS_IN_BAG);
        if (isDecimalCurrency()) {
            long addCentavos = Math.round(amount * 100.0);
            coinsInBag.valueTotalInCurrency = Math.max(0L, coinsInBag.valueTotalInCurrency + addCentavos);
        } else {
            coinsInBag.valueTotalInCoins = Math.max(0, getCoins(entity) + (int) amount);
        }
        coinsInBag.syncCoinsInBag(entity);
    }

    @Override
    public void removeCurrency(Entity entity, double amount) {
        CoinsInBag coinsInBag = entity.getData(CoinsBagCapabilities.COINS_IN_BAG);
        if (isDecimalCurrency()) {
            long removeCentavos = Math.round(amount * 100.0);
            coinsInBag.valueTotalInCurrency = Math.max(0L, coinsInBag.valueTotalInCurrency - removeCentavos);
        } else {
            coinsInBag.valueTotalInCoins = Math.max(0, getCoins(entity) - (int) amount);
        }
        coinsInBag.syncCoinsInBag(entity);
    }

}
