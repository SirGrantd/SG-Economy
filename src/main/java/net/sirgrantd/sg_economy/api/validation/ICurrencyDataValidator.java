package net.sirgrantd.sg_economy.api.validation;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

/**
 * Interface responsável pela identificação, validação, padronização e migração
 * de dados de economia (antigos capabilities, tags NBT legadas do Magic Coins / SG-Economy).
 */
public interface ICurrencyDataValidator {

    /**
     * Valida e sanitiza um valor numérico de saldo em formato interno (long).
     * Garante que não haja valores negativos ou corrupção de overflow.
     *
     * @param amount Valor bruto a ser validado.
     * @return Valor devidamente sanitizado (mínimo 0).
     */
    long sanitize(long amount);

    /**
     * Valida e sanitiza um valor numérico decimal de saldo.
     *
     * @param amount Valor decimal bruto.
     * @return Valor decimal sanitizado.
     */
    double sanitize(double amount);

    /**
     * Interpreta tags NBT detectando formatos legados (ValueTotalInCurrency, ValueTotalInCoins, etc.)
     * ou o formato padrão atual ("balance") e converte para o valor numérico padrão.
     *
     * @param nbt Tag NBT a ser inspecionada.
     * @return Saldo padronizado em formato interno.
     */
    long parseAndMigrateNbt(CompoundTag nbt);

    /**
     * Inspeciona dados persistentes legados do jogador (ex: persistentData antigo do Magic Coins
     * ou capabilities legados), migra os valores para o Data Attachment atual e remove tags obsoletas.
     *
     * @param player Jogador a ser validado e limpo.
     * @return true se alguma migração/limpeza foi executada, false caso contrário.
     */
    boolean migrateAndCleanPlayer(ServerPlayer player);
}
