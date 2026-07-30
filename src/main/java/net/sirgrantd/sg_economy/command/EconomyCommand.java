package net.sirgrantd.sg_economy.command;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.sirgrantd.sg_economy.SGEconomyMod;
import net.sirgrantd.sg_economy.api.EconomyEventProvider;
import net.sirgrantd.sg_economy.config.ServerConfig;
import net.sirgrantd.sg_economy.internal.EconomyServices;

@EventBusSubscriber
public class EconomyCommand {

    public record PlayerOnlineInfo(ServerPlayer player, String uuid) {
    }

    public record PlayerCoinsInfo(String name, double currency) {
    }

    private static int addCurrency(CommandContext<CommandSourceStack> arguments) throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(arguments, "players");
        double amount = DoubleArgumentType.getDouble(arguments, "amount");
        EconomyEventProvider economy = EconomyServices.get();

        String coinText = Component.translatable(amount == 1 ? "text.coin" : "text.coins").getString();
        String messageTemplate = Component.translatable("command.coins.add.success").getString();

        int successCount = 0;
        for (ServerPlayer player : players) {
            economy.depositBalance(player, amount);

            String message = String.format(messageTemplate, amount, player.getName().getString(), coinText);
            arguments.getSource().sendSystemMessage(Component.literal("§a" + message));
            successCount++;
        }

        return successCount;
    }

    private static int setCurrency(CommandContext<CommandSourceStack> arguments) throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(arguments, "players");
        double amount = DoubleArgumentType.getDouble(arguments, "amount");
        EconomyEventProvider economy = EconomyServices.get();

        String coinText = Component.translatable(amount == 1 ? "text.coin" : "text.coins").getString();
        String messageTemplate = Component.translatable("command.coins.set.success").getString();

        int successCount = 0;
        for (ServerPlayer player : players) {
            economy.setBalance(player, amount);

            String message = String.format(messageTemplate, amount, player.getName().getString(), coinText);
            arguments.getSource().sendSystemMessage(Component.literal("§a" + message));
            successCount++;
        }

        return successCount;
    }

    private static int removeCurrency(CommandContext<CommandSourceStack> arguments) throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(arguments, "players");
        double amount = DoubleArgumentType.getDouble(arguments, "amount");
        EconomyEventProvider economy = EconomyServices.get();

        String coinText = Component.translatable(amount == 1 ? "text.coin" : "text.coins").getString();
        String messageTemplate = Component.translatable("command.coins.remove.success").getString();

        int successCount = 0;
        for (ServerPlayer player : players) {
            if (!economy.hasBalance(player, amount)) {
                arguments.getSource()
                        .sendSystemMessage(Component.translatable("command.coins.exception.insufficient_funds"));
                continue;
            }

            economy.withdrawBalance(player, amount);
            String message = String.format(messageTemplate, amount, player.getName().getString(), coinText);
            arguments.getSource().sendSystemMessage(Component.literal("§a" + message));
            successCount++;
        }

        return successCount;
    }

    private static int getCurrency(CommandContext<CommandSourceStack> arguments, Entity targetPlayer) {
        if (targetPlayer == null) {
            targetPlayer = arguments.getSource().getEntity();
            if (!(targetPlayer instanceof ServerPlayer)) {
                arguments.getSource().sendSystemMessage(Component.translatable("command.coins.exception.only_players"));
                return 0;
            }
        }

        EconomyEventProvider economy = EconomyServices.get();
        if (economy.isDecimalSystem()) {
            double balance = economy.getBalance(targetPlayer);
            String balanceString = String.format("%.2f", balance);

            arguments.getSource().sendSystemMessage(Component.literal(
                    String.format("%s: §a$%s", targetPlayer.getName().getString(), balanceString)));
        } else {
            int balance = (int) economy.getBalance(targetPlayer);

            arguments.getSource().sendSystemMessage(Component.literal(
                    String.format("%s: §a$%d", targetPlayer.getName().getString(), balance)));
        }

        return 1;
    }

    private static int payCurrency(CommandContext<CommandSourceStack> arguments) throws CommandSyntaxException {
        if (!ServerConfig.isActivePayCommand) {
            arguments.getSource().sendSystemMessage(Component.translatable("command.coins.exception.disabled"));
            return 0;
        }

        Entity senderEntity = arguments.getSource().getEntity();
        if (!(senderEntity instanceof ServerPlayer sender)) {
            arguments.getSource().sendSystemMessage(Component.translatable("command.coins.exception.only_players"));
            return 0;
        }

        ServerPlayer target = EntityArgument.getPlayer(arguments, "target");
        double amount = DoubleArgumentType.getDouble(arguments, "amount");

        EconomyEventProvider economy = EconomyServices.get();

        double senderBalance = economy.isDecimalSystem()
                ? economy.getBalance(sender)
                : (int) economy.getBalance(sender);

        if (senderBalance < amount) {
            arguments.getSource()
                    .sendSystemMessage(Component.translatable("command.coins.exception.insufficient_funds"));
            return 0;
        }

        if (economy.isDecimalSystem()) {
            economy.transferBalance(senderEntity, target, amount);
        } else {
            int intAmount = (int) Math.round(amount);
            economy.transferBalance(senderEntity, target, intAmount);
        }

        String coinText = Component.translatable(amount == 1 ? "text.coin" : "text.coins").getString();

        String sentMsg = String.format("§aYou sent %s %s to %s.",
                economy.isDecimalSystem() ? String.format("%.2f", amount) : String.format("%d", Math.round(amount)),
                coinText,
                target.getName().getString());
        String receivedMsg = String.format("§aYou received %s %s from %s.",
                economy.isDecimalSystem() ? String.format("%.2f", amount) : String.format("%d", Math.round(amount)),
                coinText,
                sender.getName().getString());

        arguments.getSource().sendSystemMessage(Component.literal(sentMsg));
        target.sendSystemMessage(Component.literal(receivedMsg));
        return 1;
    }

    private static int rankCoins(CommandContext<CommandSourceStack> arguments, int page) {
        MinecraftServer server = arguments.getSource().getServer();

        Map<String, PlayerOnlineInfo> playersOnlineMap = server.getPlayerList().getPlayers().stream()
                .map(p -> new PlayerOnlineInfo(p, p.getUUID().toString()))
                .collect(Collectors.toMap(PlayerOnlineInfo::uuid, p -> p));

        File playerDataFolder = server.getWorldPath(LevelResource.PLAYER_DATA_DIR).toFile();
        List<PlayerCoinsInfo> ranking = new ArrayList<>();

        EconomyEventProvider economy = EconomyServices.get();
        boolean isDecimalSystem = economy.isDecimalSystem();

        for (PlayerOnlineInfo onlineInfo : playersOnlineMap.values()) {
            Entity playerEntity = onlineInfo.player();
            double currency = economy.getBalance(playerEntity);
            String name = playerEntity.getName().getString();
            ranking.add(new PlayerCoinsInfo(name, currency));
        }

        File[] files = playerDataFolder.listFiles((dir, name) -> name.endsWith(".dat"));
        if (files != null) {
            for (File file : files) {
                try {
                    String fileUUID = file.getName().replace(".dat", "");
                    if (playersOnlineMap.containsKey(fileUUID))
                        continue;

                    double currency = 0.0;
                    String name = fileUUID;
                    CompoundTag nbt = NbtIo.readCompressed(file.toPath(), NbtAccounter.unlimitedHeap());
                    UUID uuid = UUID.fromString(fileUUID);
                    name = server.getProfileCache().get(uuid).map(GameProfile::getName).orElse(uuid.toString());

                    if (nbt.contains("neoforge:attachments")) {
                        CompoundTag attachments = nbt.getCompound("neoforge:attachments");
                        String key = SGEconomyMod.MG_COINS_ID + ":coins_in_bag";
                        if (attachments.contains(key)) {
                            CompoundTag coinsNbt = attachments.getCompound(key);
                            if (isDecimalSystem) {

                                if (coinsNbt.contains("ValueTotalInCurrency", CompoundTag.TAG_LONG)) {
                                    currency = coinsNbt.getLong("ValueTotalInCurrency") / 100.0;
                                } else if (coinsNbt.contains("ValueTotalInCurrency", CompoundTag.TAG_DOUBLE)) {
                                    currency = coinsNbt.getDouble("ValueTotalInCurrency");
                                } else {
                                    currency = 0.0;
                                }

                            } else {
                                currency = coinsNbt.getInt("ValueTotalInCoins");
                            }
                        }
                    }

                    ranking.add(new PlayerCoinsInfo(name, currency));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        ranking.sort((a, b) -> Double.compare(b.currency(), a.currency()));

        int pageSize = 10;
        int totalPages = (int) Math.ceil((double) ranking.size() / pageSize);

        if (page < 1)
            page = 1;
        if (page > totalPages)
            page = totalPages;

        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, ranking.size());

        String title = Component.translatable("command.coins.rank.title").getString();
        String pageText = Component.translatable("text.page").getString();
        arguments.getSource().sendSystemMessage(Component.literal(
                String.format("§6-> %s (%s %d/%d) <-", title, pageText, page, totalPages)));

        for (int i = start; i < end; i++) {
            PlayerCoinsInfo info = ranking.get(i);
            String valueStr = isDecimalSystem
                    ? String.format("$%.2f", info.currency())
                    : String.format("$%d", (int) info.currency());
            arguments.getSource().sendSystemMessage(Component.literal(
                    String.format("§6%d. §f%s: §a%s", i + 1, info.name(), valueStr)));
        }

        return 1;
    }

    @SubscribeEvent
    public static void registerCoinsCommand(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("coins")

                .then(Commands.literal("add")
                        .requires(s -> s.hasPermission(2)) // Alterado de 4 para 2 (Permite Command Blocks)
                        .then(Commands.argument("players", EntityArgument.players())
                                .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0))
                                        .executes(EconomyCommand::addCurrency))))

                .then(Commands.literal("set")
                        .requires(s -> s.hasPermission(2)) // Alterado de 4 para 2 (Permite Command Blocks)
                        .then(Commands.argument("players", EntityArgument.players())
                                .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0))
                                        .executes(EconomyCommand::setCurrency))))

                .then(Commands.literal("remove")
                        .requires(s -> s.hasPermission(2)) // Alterado de 4 para 2 (Permite Command Blocks)
                        .then(Commands.argument("players", EntityArgument.players())
                                .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0))
                                        .executes(EconomyCommand::removeCurrency))))

                .then(Commands.literal("get")
                        .requires(s -> s.hasPermission(0))
                        .executes(arguments -> getCurrency(arguments, null))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(arguments -> {
                                    Entity player = EntityArgument.getEntity(arguments, "player");
                                    return getCurrency(arguments, player);
                                })))

                .then(Commands.literal("pay")
                        .requires(s -> s.hasPermission(0))
                        .then(Commands.argument("target", EntityArgument.player())
                                .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.01))
                                        .executes(EconomyCommand::payCurrency))))

                .then(Commands.literal("rank")
                        .executes(arguments -> rankCoins(arguments, 1))
                        .then(Commands.argument("page", IntegerArgumentType.integer(0))
                                .requires(s -> s.hasPermission(0))
                                .executes(arguments -> {
                                    int page = IntegerArgumentType.getInteger(arguments, "page");
                                    return rankCoins(arguments, page);
                                }))));
    }
}