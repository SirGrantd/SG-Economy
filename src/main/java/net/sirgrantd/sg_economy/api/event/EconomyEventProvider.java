package net.sirgrantd.sg_economy.api.event;

import net.minecraft.world.entity.Entity;

public interface EconomyEventProvider {
    boolean isCoinsLostOnDeath(Entity entity);
    void setCoinsLostOnDeath(Entity entity, boolean lost);
    int getPercentageCoinsSaveOnDeath();
}
