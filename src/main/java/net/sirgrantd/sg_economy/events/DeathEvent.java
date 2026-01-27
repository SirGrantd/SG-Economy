package net.sirgrantd.sg_economy.events;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.sirgrantd.sg_economy.internal.DefaultEconomyProvider;
import net.sirgrantd.sg_economy.internal.DeathEventEconomyProvider;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber
public class DeathEvent {

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        Entity entity = event.getEntity();

        if (entity instanceof Player player) {
            handlePlayerDeath(player);
        }
    }

    private static void handlePlayerDeath(Player player) {
        boolean isCoinsLostOnDeath = DeathEventEconomyProvider.INSTANCE.isCoinsLostOnDeath(player);
        double percentageCoinsSaveOnDeath = DeathEventEconomyProvider.INSTANCE.getPercentageCoinsSaveOnDeath() / 100.0;
        System.out.println("Is coins lost on death: " + isCoinsLostOnDeath);
        System.out.println("Percentage coins saved on death: " + percentageCoinsSaveOnDeath);

        if (DefaultEconomyProvider.INSTANCE.isDecimalCurrency()) {
            double balance = DefaultEconomyProvider.INSTANCE.getCurrency(player);
            double coinsToSave = isCoinsLostOnDeath ? balance * percentageCoinsSaveOnDeath : balance;
            System.out.println("Setting currency to: " + coinsToSave);
            DefaultEconomyProvider.INSTANCE.setCurrency(player, coinsToSave);
        } else {
            int balance = DefaultEconomyProvider.INSTANCE.getCoins(player);
            int coinsToSave = isCoinsLostOnDeath ? (int) Math.round(balance * percentageCoinsSaveOnDeath) : balance;
            System.out.println("Setting coins to: " + coinsToSave);
            DefaultEconomyProvider.INSTANCE.setCoins(player, coinsToSave);
        }
    }
}