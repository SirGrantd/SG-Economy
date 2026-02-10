package net.sirgrantd.sg_economy.internal;

import net.minecraft.world.entity.Entity;
import net.sirgrantd.sg_economy.SGEconomyMod;
import net.sirgrantd.sg_economy.api.EconomyEventProvider;
import net.sirgrantd.sg_economy.config.ServerConfig;
import net.sirgrantd.sg_economy.repositories.CoinsBagRepository;

public class EconomyEventServiceImpl implements EconomyEventProvider {

    @Override
    public boolean isDecimalSystem() {
        return ServerConfig.isDecimalCurrency;
    }

    @Override
    public boolean hasCoinsBag(Entity entity) {
        return CoinsBagRepository.hasCoinsBag(entity);
    }

    @Override
    public boolean hasBalance(Entity entity, double balance) {
        return getBalance(entity) >= balance;
    }

    @Override
    public double getBalance(Entity entity) {
        if (isDecimalSystem()) {
            return CoinsBagRepository.getCurrency(entity) / 100.0;
        }
        return (double) CoinsBagRepository.getCoins(entity);
    }

    @Override
    public boolean setBalance(Entity entity, double amount) {
        if (amount < 0) {
            SGEconomyMod.LOGGER.warn("Attempted to set a negative balance: " + amount);
            return false;
        }
        if (isDecimalSystem()) {
            long amountInCents = Math.round(amount * 100.0);
            CoinsBagRepository.setCurrency(entity, amountInCents);
        } else {
            CoinsBagRepository.setCoins(entity, (int) amount);
        }
        return true;
    }

    @Override
    public boolean depositBalance(Entity entity, double amount) {
        if (amount < 0) {
            SGEconomyMod.LOGGER.warn("Attempted to deposit a negative amount: " + amount);
            return false;
        }
        if (isDecimalSystem()) {
            long amountInCents = Math.round(amount * 100.0);
            CoinsBagRepository.addCurrency(entity, amountInCents);
            return true;
        }
        CoinsBagRepository.addCoins(entity, (int) amount);
        return true;
    }

    @Override
    public boolean withdrawBalance(Entity entity, double amount) {
        if (amount < 0) {
            SGEconomyMod.LOGGER.warn("Attempted to withdraw a negative amount: " + amount);
            return false;
        }
        if (!hasBalance(entity, amount)) {
            SGEconomyMod.LOGGER.warn("Attempted to withdraw more than the available balance: " + amount);
            return false;
        }
        if (isDecimalSystem()) {
            long amountInCents = Math.round(amount * 100.0);
            CoinsBagRepository.removeCurrency(entity, amountInCents);
            return true;
        }
        CoinsBagRepository.removeCoins(entity, (int) amount);
        return true;
    }

    @Override
    public boolean transferBalance(Entity from, Entity to, double amount) {
        if (amount < 0) {
            SGEconomyMod.LOGGER.warn("Attempted to transfer a negative amount: " + amount);
            return false;
        }
        if (!hasBalance(from, amount)) {
            SGEconomyMod.LOGGER.warn("Attempted to transfer more than the available balance: " + amount);
            return false;
        }
        if (isDecimalSystem()) {
            long amountInCents = Math.round(amount * 100.0);
            CoinsBagRepository.removeCurrency(from, amountInCents);
            CoinsBagRepository.addCurrency(to, amountInCents);
            return true;
        }
        CoinsBagRepository.removeCoins(from, (int) amount);
        CoinsBagRepository.addCoins(to, (int) amount);
        return true;
    }

    @Override
    public boolean balanceLostOnDeath(Entity entity) {
        return CoinsBagRepository.balanceLostOnDeath(entity);
    }

    @Override
    public boolean setBalanceLostOnDeath(Entity entity, boolean lost) {
        CoinsBagRepository.setBalanceLostOnDeath(entity, lost);
        return true;
    }

    @Override
    public int getPercentageBalanceSaveOnDeath() {
        return ServerConfig.percentageCoinsSaveOnDeath;
    }

}
