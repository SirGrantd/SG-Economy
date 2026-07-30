package net.sirgrantd.sg_economy.repositories;

import net.minecraft.world.entity.Entity;
import net.sirgrantd.sg_economy.capabilities.CoinsBagCapabilities;
import net.sirgrantd.sg_economy.capabilities.CoinsBagCapabilities.CoinsInBag;

public class CoinsBagRepository {

    private static CoinsInBag coinsInBag(Entity entity) {
        return entity.getData(CoinsBagCapabilities.COINS_IN_BAG);
    }
    public static boolean hasCoinsBag(Entity entity) {
        return coinsInBag(entity) != null;
    }
    
    // System Decimal methods
    public static long getCurrency(Entity entity) {
        return coinsInBag(entity).valueTotalInCurrency;
    }
    public static void setCurrency(Entity entity, long amount) {
        CoinsInBag coinsInBag = coinsInBag(entity);
        coinsInBag.valueTotalInCurrency = Math.max(0L, amount);
        coinsInBag.syncCoinsInBag(entity);
    }
    public static void addCurrency(Entity entity, long amount) {
        CoinsInBag coinsInBag = coinsInBag(entity);
        coinsInBag.valueTotalInCurrency = Math.max(0L, coinsInBag.valueTotalInCurrency + amount);
        coinsInBag.syncCoinsInBag(entity);
    }
    public static void removeCurrency(Entity entity, long amount) {
        CoinsInBag coinsInBag = coinsInBag(entity);
        coinsInBag.valueTotalInCurrency = Math.max(0L, coinsInBag.valueTotalInCurrency - amount);
        coinsInBag.syncCoinsInBag(entity);
    }

    // System Integer methods
    public static int getCoins(Entity entity) {
        return coinsInBag(entity).valueTotalInCoins;
    }
    public static void setCoins(Entity entity, int amount) {
        CoinsInBag coinsInBag = coinsInBag(entity);
        coinsInBag.valueTotalInCoins = Math.max(0, amount);
        coinsInBag.syncCoinsInBag(entity);
    }
    public static void addCoins(Entity entity, int amount) {
        CoinsInBag coinsInBag = coinsInBag(entity);
        coinsInBag.valueTotalInCoins = Math.max(0, coinsInBag.valueTotalInCoins + amount);
        coinsInBag.syncCoinsInBag(entity);
    }
    public static void removeCoins(Entity entity, int amount) {
        CoinsInBag coinsInBag = coinsInBag(entity);
        coinsInBag.valueTotalInCoins = Math.max(0, coinsInBag.valueTotalInCoins - amount);
        coinsInBag.syncCoinsInBag(entity);
    }

    // Events Death
    public static boolean balanceLostOnDeath(Entity entity) {
        return coinsInBag(entity).isCoinsLostOnDeath;
    }
    public static void setBalanceLostOnDeath(Entity entity, boolean lost) {
        CoinsInBag coinsInBag = coinsInBag(entity);
        coinsInBag.isCoinsLostOnDeath = lost;
        coinsInBag.syncCoinsInBag(entity);
    }

}
