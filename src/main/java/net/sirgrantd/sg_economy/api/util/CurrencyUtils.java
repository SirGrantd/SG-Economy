package net.sirgrantd.sg_economy.api.util;

import net.minecraft.world.entity.Entity;
import net.sirgrantd.sg_economy.internal.DefaultEconomyProvider;

public class CurrencyUtils {

    public static boolean hasBalance(Entity entity, double amount) {
        if (amount < 0) {
            return false;
        }
        double balance = DefaultEconomyProvider.INSTANCE.getCurrency(entity);
        return balance >= amount;
    }

}
