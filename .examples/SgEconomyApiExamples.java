package examples;

import net.minecraft.world.entity.Entity;
import net.sirgrantd.sg_economy.api.EconomyEventProvider;
import net.sirgrantd.sg_economy.api.SGEconomyApi;

public class SgEconomyApiExamples {

    public void examples(Entity player, Entity target) {

        EconomyEventProvider economy = SGEconomyApi.get();

        // SYSTEM CONFIGURATION

        boolean isDecimal = economy.isDecimalSystem();
        boolean hasWallet = economy.hasCoinsBag(player);

        // BALANCE QUERIES

        double balance = economy.getBalance(player);
        boolean hasEnough = economy.hasBalance(player, 20.0);

        // OPERATIONS WITH DOUBLE

        economy.depositBalance(player, 30.5);
        economy.withdrawBalance(player, 5.7);
        economy.setBalance(player, 100.0);

        // OPERATIONS WITH INT (when not a decimal system)

        int coins = economy.getBalanceAsInt(player);
        economy.depositBalanceAsInt(player, 15);
        economy.withdrawBalanceAsInt(player, 8);
        economy.setBalanceAsInt(player, 55);

        // BALANCE TRANSFER BETWEEN ENTITIES

        economy.transferBalance(player, target, 10.0);
        economy.transferBalanceAsInt(player, target, 5);

        // BEHAVIOR ON DEATH

        boolean losesBalance = economy.balanceLostOnDeath(player);
        economy.setBalanceLostOnDeath(player, true);

        int percentageSaved = economy.getPercentageBalanceSaveOnDeath();
    }
}
