package net.sirgrantd.sg_economy.api;

import net.minecraft.world.entity.Entity;

/**
 * Interface responsible for providing economy operations
 * associated with game entities.
 */
public interface EconomyEventProvider {

    /**
     * Indicates whether the monetary system uses decimal values.
     *
     * @return true if the system is decimal, false if it is integer
     */
    boolean isDecimalSystem();

    /**
     * Checks if the entity has a coins bag/wallet.
     *
     * @param entity entity to be checked
     * @return true if it has a coins bag
     */
    boolean hasCoinsBag(Entity entity);

    /**
     * Checks if the entity has at least the informed balance.
     *
     * @param entity entity to be checked
     * @param balance minimum expected value
     * @return true if the balance is sufficient
     */
    boolean hasBalance(Entity entity, double balance);

    /**
     * Gets the current balance of the entity.
     *
     * @param entity entity
     * @return current balance
     */
    double getBalance(Entity entity);

    /**
     * Directly sets the balance of the entity.
     *
     * @param entity entity
     * @param amount new balance value
     * @return true if the operation was successful
     */
    boolean setBalance(Entity entity, double amount);

    /**
     * Deposits a value to the entity's balance.
     *
     * @param entity entity
     * @param amount value to be added
     * @return true if the operation was successful
     */
    boolean depositBalance(Entity entity, double amount);

    /**
     * Withdraws a value from the entity's balance.
     *
     * @param entity entity
     * @param amount value to be removed
     * @return true if the operation was successful
     */
    boolean withdrawBalance(Entity entity, double amount);

    /**
     * Transfers a value from one entity's balance to another.
     *
     * @param from source entity
     * @param to destination entity
     * @param amount value to be transferred
     * @return true if the transfer was successful
     */
    boolean transferBalance(Entity from, Entity to, double amount);

    /**
     * Gets the entity's balance as an integer.
     * Useful when the system does not use decimals.
     *
     * @param entity entity
     * @return balance converted to int
     */
    default int getBalanceAsInt(Entity entity) {
        return (int) getBalance(entity);
    }

    /**
     * Sets the entity's balance using an integer value.
     *
     * @param entity entity
     * @param amount integer balance value
     * @return true if the operation was successful
     */
    default boolean setBalanceAsInt(Entity entity, int amount) {
        return setBalance(entity, (double) amount);
    }

    /**
     * Deposits an integer value to the entity's balance.
     *
     * @param entity entity
     * @param amount value to be deposited
     * @return true if the operation was successful
     */
    default boolean depositBalanceAsInt(Entity entity, int amount) {
        return depositBalance(entity, (double) amount);
    }

    /**
     * Withdraws an integer value from the entity's balance.
     *
     * @param entity entity
     * @param amount value to be removed
     * @return true if the operation was successful
     */
    default boolean withdrawBalanceAsInt(Entity entity, int amount) {
        return withdrawBalance(entity, (double) amount);
    }

    /**
     * Transfers an integer value between entities.
     *
     * @param from source entity
     * @param to destination entity
     * @param amount value to be transferred
     * @return true if the transfer was successful
     */
    default boolean transferBalanceAsInt(Entity from, Entity to, int amount) {
        return transferBalance(from, to, (double) amount);
    }

    /**
     * Checks if the entity loses balance when dying.
     *
     * @param entity entity
     * @return true if the balance is lost on death
     */
    boolean balanceLostOnDeath(Entity entity);

    /**
     * Sets whether the entity loses balance when dying.
     *
     * @param entity entity
     * @param lost true to lose balance on death
     * @return true if the operation was successful
     */
    boolean setBalanceLostOnDeath(Entity entity, boolean lost);

    /**
     * Returns the percentage of balance that is preserved after death.
     *
     * @return percentage of balance saved (0–100)
     */
    int getPercentageBalanceSaveOnDeath();
}
