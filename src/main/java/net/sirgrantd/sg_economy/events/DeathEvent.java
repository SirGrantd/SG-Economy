package net.sirgrantd.sg_economy.events;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.sirgrantd.sg_economy.SGEconomyMod;
import net.sirgrantd.sg_economy.api.EconomyEventProvider;
import net.sirgrantd.sg_economy.internal.EconomyServices;
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
        EconomyEventProvider economy = EconomyServices.get();
        boolean balanceLostOnDeath = economy.balanceLostOnDeath(player);
        double percentageBalanceSaveOnDeath = economy.getPercentageBalanceSaveOnDeath() / 100.0;

        if (economy.isDecimalSystem()) {
            double balance = economy.getBalance(player);
            double balanceToSave = balanceLostOnDeath ? balance * percentageBalanceSaveOnDeath : balance;
            economy.setBalance(player, balanceToSave);
            SGEconomyMod.LOGGER.debug("Player {} died. Original balance: {}, Balance to save: {}", player.getName().getString(), balance, balanceToSave);
        } else {
            int balance = economy.getBalanceAsInt(player);
            int balanceToSave = balanceLostOnDeath ? (int) Math.round(balance * percentageBalanceSaveOnDeath) : balance;
            economy.setBalanceAsInt(player, balanceToSave);
            SGEconomyMod.LOGGER.debug("Player {} died. Original balance: {}, Balance to save: {}", player.getName().getString(), balance, balanceToSave);
        }
    }
}