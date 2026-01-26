package net.sirgrantd.sg_economy.api.economy;

import net.minecraft.world.entity.Entity;

public interface SGEconomyProvider {
    boolean hasCapability(Entity entity);
    double getCurrency(Entity entity);
    void setCurrency(Entity entity, double amount);
    void addCurrency(Entity entity, double amount);
    void removeCurrency(Entity entity, double amount);

    default int getCoins(Entity entity) {
        return (int) getCurrency(entity);
    }
    default void setCoins(Entity entity, int amount) {
        setCurrency(entity, (double) amount);
    }
    default void addCoins(Entity entity, int amount) {
        addCurrency(entity, (double) amount);
    }
    default void removeCoins(Entity entity, int amount) {
        removeCurrency(entity, (double) amount);
    }
}

