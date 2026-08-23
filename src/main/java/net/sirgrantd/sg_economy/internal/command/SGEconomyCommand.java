package net.sirgrantd.sg_economy.internal.command;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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
import net.neoforged.neoforge.common.UsernameCache;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.sirgrantd.sg_economy.SGEconomyMod;
import net.sirgrantd.sg_economy.api.SGEconomyApi;
import net.sirgrantd.sg_economy.internal.config.ServerConfig;

@EventBusSubscriber
public class SGEconomyCommand {
    public record PlayerOnlineInfo(ServerPlayer player, String uuid) {
    }

    public record PlayerCoinsInfo(String name, double currency) {
    }

    private static double getDynamicAmount(CommandContext<CommandSourceStack> arguments, boolean enforceMinimum)
            throws CommandSyntaxException {
        double amount = DoubleArgumentType.getDouble(arguments, "amount");

        if (!SGEconomyApi.isDecimalSystem()) {
            amount = Math.floor(amount);

            if (enforceMinimum && amount < 1.0) {
                arguments.getSource().sendSystemMessage(
                        Component.literal("§cO valor mínimo permitido para sistemas não-decimais é 1."));
                return 0.0;
            }
        }
        return amount;
    }

    private static int addCurrency(CommandContext<CommandSourceStack> arguments) throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(arguments, "players");

        double amount = getDynamicAmount(arguments, true);
        if (amount <= 0)
            return 0;

        String coinText = Component.translatable(amount == 1 ? "text.coin" : "text.coins").getString();
        String messageTemplate = Component.translatable("command.coins.add.success").getString();

        int successCount = 0;
        for (ServerPlayer player : players) {
            SGEconomyApi.depositBalance(player, amount);

            String balanceString = SGEconomyApi.isDecimalSystem()
                    ? String.format("%.2f", amount)
                    : String.format("%d", (long) amount);

            String message = String.format(messageTemplate, balanceString, player.getName().getString(), coinText);
            arguments.getSource().sendSystemMessage(Component.literal("§a" + message));
            successCount++;
        }

        return successCount;
    }

    private static int setCurrency(CommandContext<CommandSourceStack> arguments) throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(arguments, "players");

        double amount = getDynamicAmount(arguments, false);

        String coinText = Component.translatable(amount == 1 ? "text.coin" : "text.coins").getString();
        String messageTemplate = Component.translatable("command.coins.set.success").getString();

        int successCount = 0;
        for (ServerPlayer player : players) {
            SGEconomyApi.setBalance(player, amount);

            String balanceString = SGEconomyApi.isDecimalSystem()
                    ? String.format("%.2f", amount)
                    : String.format("%d", (long) amount);

            String message = String.format(messageTemplate, balanceString, player.getName().getString(), coinText);
            arguments.getSource().sendSystemMessage(Component.literal("§a" + message));
            successCount++;
        }

        return successCount;
    }

    private static int removeCurrency(CommandContext<CommandSourceStack> arguments) throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(arguments, "players");

        double amount = getDynamicAmount(arguments, true);
        if (amount <= 0)
            return 0;

        String coinText = Component.translatable(amount == 1 ? "text.coin" : "text.coins").getString();
        String messageTemplate = Component.translatable("command.coins.remove.success").getString();

        int successCount = 0;
        for (ServerPlayer player : players) {
            if (!SGEconomyApi.hasBalance(player, amount)) {
                arguments.getSource()
                        .sendSystemMessage(Component.translatable("command.coins.exception.insufficient_funds"));
                continue;
            }
            SGEconomyApi.withdrawBalance(player, amount);

            String balanceString = SGEconomyApi.isDecimalSystem()
                    ? String.format("%.2f", amount)
                    : String.format("%d", (long) amount);

            String message = String.format(messageTemplate, balanceString, player.getName().getString(), coinText);
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

        double balance = SGEconomyApi.getBalance(targetPlayer);
        String balanceString = SGEconomyApi.isDecimalSystem() ? String.format("%.2f", balance)
                : String.format("%d", (long) balance);

        arguments.getSource().sendSystemMessage(Component.literal(
                String.format("%s: §a$%s", targetPlayer.getName().getString(), balanceString)));

        return 1;
    }

    private static int payCurrency(CommandContext<CommandSourceStack> arguments) throws CommandSyntaxException {
        if (!ServerConfig.isEnablePayCommand) {
            arguments.getSource().sendSystemMessage(Component.translatable("command.coins.exception.disabled"));
            return 0;
        }

        Entity senderEntity = arguments.getSource().getEntity();
        if (!(senderEntity instanceof ServerPlayer sender)) {
            arguments.getSource().sendSystemMessage(Component.translatable("command.coins.exception.only_players"));
            return 0;
        }

        ServerPlayer target = EntityArgument.getPlayer(arguments, "target");

        double amount = getDynamicAmount(arguments, true);
        if (amount <= 0)
            return 0;

        if (!SGEconomyApi.transferBalance(senderEntity, target, amount)) {
            arguments.getSource()
                    .sendSystemMessage(Component.translatable("command.coins.exception.insufficient_funds"));
            return 0;
        }

        String coinText = Component.translatable(amount == 1 ? "text.coin" : "text.coins").getString();

        String balanceString = SGEconomyApi.isDecimalSystem() ? String.format("%.2f", amount)
                : String.format("%d", (long) amount);

        String sentMsg = Component.translatable("command.coins.pay.sent").getString();
        String receivedMsg = Component.translatable("command.coins.pay.received").getString();

        sentMsg = String.format(sentMsg, balanceString, coinText, target.getName().getString());
        receivedMsg = String.format(receivedMsg, balanceString, coinText, sender.getName().getString());

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

        boolean isDecimalSystem = SGEconomyApi.isDecimalSystem();

        for (PlayerOnlineInfo onlineInfo : playersOnlineMap.values()) {
            Entity playerEntity = onlineInfo.player();
            double currency = SGEconomyApi.getBalance(playerEntity);
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
                    UUID uuid = UUID.fromString(fileUUID);

                    String name = UsernameCache.getLastKnownUsername(uuid);

                    if (name == null) {
                        name = fileUUID;
                    }

                    CompoundTag nbt = NbtIo.readCompressed(file.toPath(), NbtAccounter.unlimitedHeap());
                    long internalBalance = 0L;

                    if (nbt.contains("neoforge:attachments", 10)) { // 10 = TAG_COMPOUND
                        CompoundTag attachments = nbt.getCompound("neoforge:attachments");
                        if (attachments.contains(SGEconomyMod.MOD_ID + ":currency_player", 10)) {
                            CompoundTag coinsNbt = attachments.getCompound(SGEconomyMod.MOD_ID + ":currency_player");
                            internalBalance = net.sirgrantd.sg_economy.internal.validation.CurrencyDataValidator.getInstance().parseAndMigrateNbt(coinsNbt);
                        }
                    }

                    if (internalBalance == 0L) {
                        internalBalance = net.sirgrantd.sg_economy.internal.validation.CurrencyDataValidator.getInstance().parseAndMigrateNbt(nbt);
                    }

                    if (isDecimalSystem) {
                        currency = internalBalance / 100.0;
                    } else {
                        currency = (double) internalBalance;
                    }

                    ranking.add(new PlayerCoinsInfo(name, currency));
                } catch (Exception e) {
                    SGEconomyMod.LOGGER.error("Falha ao ler o arquivo do jogador para o Rank", e);
                }
            }
        }

        ranking.sort((a, b) -> Double.compare(b.currency(), a.currency()));

        int pageSize = 10;
        int totalPages = (int) Math.ceil((double) ranking.size() / pageSize);

        if (page < 1)
            page = 1;
        if (totalPages == 0)
            totalPages = 1;
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
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("players", EntityArgument.players())
                                .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.01))
                                        .executes(SGEconomyCommand::addCurrency))))

                .then(Commands.literal("set")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("players", EntityArgument.players())
                                .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0))
                                        .executes(SGEconomyCommand::setCurrency))))

                .then(Commands.literal("remove")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("players", EntityArgument.players())
                                .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.01))
                                        .executes(SGEconomyCommand::removeCurrency))))

                .then(Commands.literal("get")
                        .requires(source -> source.hasPermission(0))
                        .executes(arguments -> getCurrency(arguments, null))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(arguments -> {
                                    Entity player = EntityArgument.getEntity(arguments, "player");
                                    return getCurrency(arguments, player);
                                })))

                .then(Commands.literal("pay")
                        .requires(source -> source.hasPermission(0))
                        .then(Commands.argument("target", EntityArgument.player())
                                .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.01))
                                        .executes(SGEconomyCommand::payCurrency))))

                .then(Commands.literal("rank")
                        .executes(arguments -> rankCoins(arguments, 1))
                        .then(Commands.argument("page", IntegerArgumentType.integer(0))
                                .requires(source -> source.hasPermission(0))
                                .executes(arguments -> {
                                    int page = IntegerArgumentType.getInteger(arguments, "page");
                                    return rankCoins(arguments, page);
                                }))));
    }
}