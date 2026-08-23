package net.sirgrantd.sg_economy.internal.attachments;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.sirgrantd.celesthyd.api.network.ISyncableAttachment;
import net.sirgrantd.sg_economy.internal.network.payloads.SyncCurrencyPlayerPayload;
import net.sirgrantd.sg_economy.internal.validation.CurrencyDataValidator;

public class CurrencyPlayerAttachment implements ISyncableAttachment {

    private long balance;

    public CurrencyPlayerAttachment() {
        this.balance = 0;
    }

    public CurrencyPlayerAttachment(long balance) {
        this.balance = CurrencyDataValidator.getInstance().sanitize(balance);
    }

    public long getCount() {
        return this.balance;
    }

    public void setBalance(long balance) {
        this.balance = CurrencyDataValidator.getInstance().sanitize(balance);
    }

    public void addBalance(long amount) {
        this.balance = CurrencyDataValidator.getInstance().sanitize(this.balance + amount);
    }

    public void removeBalance(long amount) {
        this.balance = CurrencyDataValidator.getInstance().sanitize(this.balance - amount);
    }

    @Override
    public void syncToClient(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new SyncCurrencyPlayerPayload(this.balance));
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putLong("balance", this.balance);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        this.balance = CurrencyDataValidator.getInstance().parseAndMigrateNbt(nbt);
    }

}
