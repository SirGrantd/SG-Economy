package net.sirgrantd.sg_economy.internal.attachments;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.network.PacketDistributor;
import net.sirgrantd.celesthyd.api.network.ISyncableAttachment;
import net.sirgrantd.sg_economy.internal.network.payloads.SyncCurrencyPlayerPayload;

public class CurrencyPlayerAttachment implements ISyncableAttachment {

    private long balance;

    public CurrencyPlayerAttachment() {
        this.balance = 0;
    }

    public CurrencyPlayerAttachment(long balance) {
        this.balance = balance;
    }

    public long getCount() {
        return this.balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public void addBalance(long amount) {
        this.balance += amount;
    }

    public void removeBalance(long amount) {
        this.balance -= amount;
    }

    @Override
    public void syncToClient(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new SyncCurrencyPlayerPayload(this.balance));
    }

    @Override
    public void serialize(ValueOutput output) {
        output.putLong("balance", this.balance);
    }

    @Override
    public void deserialize(ValueInput input) {
        this.balance = input.getLongOr("balance", 0);
    }

}
