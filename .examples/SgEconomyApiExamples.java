package examples;

import net.sirgrantd.sg_economy.api.SGEconomyApi;
import net.sirgrantd.sg_economy.api.economy.EconomyProvider;
import net.sirgrantd.sg_economy.api.event.EconomyEventProvider;
import net.sirgrantd.sg_economy.api.util.CurrencyUtils;
import net.minecraft.world.entity.Entity;

public class SgEconomyApiExamples {

    public void examples(Entity player) {
        EconomyProvider economy = SGEconomyApi.getSGEconomy();
        EconomyEventProvider events = SGEconomyApi.getSGEconomyEvents();

        // METHODS OF EconomyProvider

        boolean decimals = economy.isDecimalCurrency();
        boolean hasCapability = economy.hasCapability(player);
        double balance = economy.getCurrency(player);
        economy.addCurrency(player, 30.5);
        economy.removeCurrency(player, 5.7);
        economy.setCurrency(player, 100.0);

        int coins = economy.getCoins(player);
        economy.addCoins(player, 15);
        economy.removeCoins(player, 8);
        economy.setCoins(player, 55);

        // METHODS OF EconomyEventProvider

        boolean losesCoins = events.isCoinsLostOnDeath(player);
        events.setCoinsLostOnDeath(player, true);
        int percentageSaved = events.getPercentageCoinsSaveOnDeath();

        // USAGE OF UTILITY

        boolean hasMoney = CurrencyUtils.hasBalance(player, 20.0);
    }
}