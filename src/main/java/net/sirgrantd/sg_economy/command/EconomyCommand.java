package net.sirgrantd.sg_economy.command;

import java.io.File;
import java.util.ArrayList;
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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.sirgrantd.sg_economy.SGEconomyMod;
import net.sirgrantd.sg_economy.api.util.CurrencyUtils;
import net.sirgrantd.sg_economy.config.ServerConfig;
import net.sirgrantd.sg_economy.internal.DefaultEconomyProvider;

@EventBusSubscriber
public class EconomyCommand {

    public record PlayerOnlineInfo(ServerPlayer player, String uuid) {
    }

    public record PlayerCoinsInfo(String name, double currency) {
    }

    private static int addCurrency(CommandContext<CommandSourceStack> arguments) {
        Level world = arguments.getSource().getLevel();
        Entity players = arguments.getSource().getEntity();

        if (players == null && world instanceof ServerLevel serverLevel) {
            players = FakePlayerFactory.getMinecraft(serverLevel);
        }

        if (players == null) {
            arguments.getSource().sendSystemMessage(Component.translatable("command.coins.exception.player_not_found"));
            return 0;
        }

        double amount = DoubleArgumentType.getDouble(arguments, "amount");
        Entity player = new Object() {
            public Entity getEntity() {
                try {
                    return EntityArgument.getEntity(arguments, "players");
                } catch (CommandSyntaxException e) {
                    return null;
                }
            }
        }.getEntity();

        if (player == null) {
            arguments.getSource().sendSystemMessage(Component.translatable("command.coins.exception.player_not_found"));
            return 0;
        }

        if (DefaultEconomyProvider.INSTANCE.isDecimalCurrency()) {
            DefaultEconomyProvider.INSTANCE.addCurrency(player, amount);
        } else {
            DefaultEconomyProvider.INSTANCE.addCoins(player, (int) Math.round(amount));
        }

        String coinText = Component.translatable(
                amount == 1 ? "text.coin" : "text.coins").getString();
        String message = Component.translatable(
                "command.coins.add.success").getString();
        message = String.format(message, amount, player.getName().getString(), coinText);
        arguments.getSource().sendSystemMessage(
                Component.literal("§a" + message));

        return 1;
    }

    private static int setCurrency(CommandContext<CommandSourceStack> arguments) {
        Level world = arguments.getSource().getUnsidedLevel();
        Entity players = arguments.getSource().getEntity();

        if (players == null && world instanceof ServerLevel serverLevel) {
            players = FakePlayerFactory.getMinecraft(serverLevel);
        }

        if (players == null) {
            arguments.getSource().sendSystemMessage(Component.translatable("command.coins.exception.player_not_found"));
            return 0;
        }

        double amount = DoubleArgumentType.getDouble(arguments, "amount");
        Entity player = new Object() {
            public Entity getEntity() {
                try {
                    return EntityArgument.getEntity(arguments, "players");
                } catch (CommandSyntaxException e) {
                    return null;
                }
            }
        }.getEntity();

        if (player == null) {
            arguments.getSource().sendSystemMessage(Component.translatable("command.coins.exception.player_not_found"));
            return 0;
        }

        if (DefaultEconomyProvider.INSTANCE.isDecimalCurrency()) {
            DefaultEconomyProvider.INSTANCE.setCurrency(player, amount);
        } else {
            DefaultEconomyProvider.INSTANCE.setCoins(player, (int) Math.round(amount));
        }

        String coinText = Component.translatable(
                amount == 1 ? "text.coin" : "text.coins").getString();
        String message = Component.translatable(
                "command.coins.set.success").getString();
        message = String.format(message, amount, player.getName().getString(), coinText);
        arguments.getSource().sendSystemMessage(
                Component.literal("§a" + message));

        return 1;
    }

    private static int removeCurrency(CommandContext<CommandSourceStack> arguments) {
        Level world = arguments.getSource().getLevel();
        Entity players = arguments.getSource().getEntity();

        if (players == null && world instanceof ServerLevel serverLevel) {
            players = FakePlayerFactory.getMinecraft(serverLevel);
        }
        if (players == null) {
            arguments.getSource().sendSystemMessage(Component.translatable("command.coins.exception.player_not_found"));
            return 0;
        }

        double amount = DoubleArgumentType.getDouble(arguments, "amount");
        Entity player = new Object() {
            public Entity getEntity() {
                try {
                    return EntityArgument.getEntity(arguments, "players");
                } catch (CommandSyntaxException e) {
                    return null;
                }
            }
        }.getEntity();

        if (player == null) {
            arguments.getSource().sendSystemMessage(Component.translatable("command.coins.exception.player_not_found"));
            return 0;
        }

        if (!CurrencyUtils.hasBalance(player, amount)) {
            arguments.getSource()
                    .sendSystemMessage(Component.translatable("command.coins.exception.insufficient_funds"));
            return 0;
        }

        if (DefaultEconomyProvider.INSTANCE.isDecimalCurrency()) {
            DefaultEconomyProvider.INSTANCE.removeCurrency(player, amount);
        } else {
            DefaultEconomyProvider.INSTANCE.removeCoins(player, (int) Math.round(amount));
        }

        String coinText = Component.translatable(
                amount == 1 ? "text.coin" : "text.coins").getString();
        String message = Component.translatable(
                "command.coins.remove.success").getString();
        message = String.format(message, amount, player.getName().getString(), coinText);
        arguments.getSource().sendSystemMessage(
                Component.literal("§a" + message));

        return 1;
    }

    private static int getCurrency(CommandContext<CommandSourceStack> arguments, Entity player) {
        Level world = arguments.getSource().getUnsidedLevel();
        Entity players = arguments.getSource().getEntity();

        if (players == null && world instanceof ServerLevel serverLevel) {
            players = FakePlayerFactory.getMinecraft(serverLevel);
        }

        if (players == null) {
            arguments.getSource().sendSystemMessage(Component.translatable("command.coins.exception.only_players"));
            return 0;
        }

        if (player == null) {
            player = arguments.getSource().getEntity();

            if (player == null && arguments.getSource().getUnsidedLevel() instanceof ServerLevel serverLevel) {
                player = FakePlayerFactory.getMinecraft(serverLevel);
            }
        }

        if (player == null) {
            arguments.getSource().sendSystemMessage(Component.translatable("command.coins.exception.player_not_found"));
            return 0;
        }

        if (DefaultEconomyProvider.INSTANCE.isDecimalCurrency()) {
            double balance = DefaultEconomyProvider.INSTANCE.getCurrency(player);
            String balanceString = String.format("%.2f", balance);

            arguments.getSource().sendSystemMessage(Component.literal(
                    String.format("%s: §a$%s", player.getName().getString(), balanceString)));
        } else {
            int balance = DefaultEconomyProvider.INSTANCE.getCoins(player);

            arguments.getSource().sendSystemMessage(Component.literal(
                    String.format("%s: §a$%d", player.getName().getString(), balance)));
        }

        return 1;
    }

    private static int payCurrency(CommandContext<CommandSourceStack> arguments) {
        if (!ServerConfig.isActivePayCommand) {
            arguments.getSource().sendSystemMessage(Component.translatable("command.coins.exception.disabled"));
            return 0;
        }

        Entity senderEntity = arguments.getSource().getEntity();
        if (!(senderEntity instanceof ServerPlayer sender)) {
            arguments.getSource().sendSystemMessage(Component.translatable("command.coins.exception.only_players"));
            return 0;
        }

        Entity targetEntity = new Object() {
            public Entity getEntity() {
                try {
                    return EntityArgument.getEntity(arguments, "target");
                } catch (CommandSyntaxException e) {
                    return null;
                }
            }
        }.getEntity();

        if (!(targetEntity instanceof ServerPlayer target)) {
            arguments.getSource()
                    .sendSystemMessage(Component.translatable("command.coins.exception.target_not_online"));
            return 0;
        }

        double amount = DoubleArgumentType.getDouble(arguments, "amount");

        boolean isDecimal = DefaultEconomyProvider.INSTANCE.isDecimalCurrency();

        double senderBalance = isDecimal
                ? DefaultEconomyProvider.INSTANCE.getCurrency(sender)
                : DefaultEconomyProvider.INSTANCE.getCoins(sender);

        if (senderBalance < amount) {
            arguments.getSource()
                    .sendSystemMessage(Component.translatable("command.coins.exception.insufficient_funds"));
            return 0;
        }

        if (isDecimal) {
            DefaultEconomyProvider.INSTANCE.removeCurrency(sender, amount);
            DefaultEconomyProvider.INSTANCE.addCurrency(target, amount);
        } else {
            int intAmount = (int) Math.round(amount);
            DefaultEconomyProvider.INSTANCE.removeCoins(sender, intAmount);
            DefaultEconomyProvider.INSTANCE.addCoins(target, intAmount);
        }

        String coinText = Component.translatable(
                amount == 1 ? "text.coin" : "text.coins").getString();

        String sentMsg = String.format("§aYou sent %s %s to %s.",
                isDecimal ? String.format("%.2f", amount) : String.format("%d", Math.round(amount)), coinText,
                target.getName().getString());
        String receivedMsg = String.format("§aYou received %s %s from %s.",
                isDecimal ? String.format("%.2f", amount) : String.format("%d", Math.round(amount)), coinText,
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

        boolean useDecimal = DefaultEconomyProvider.INSTANCE.isDecimalCurrency();

        for (PlayerOnlineInfo onlineInfo : playersOnlineMap.values()) {
            Entity playerEntity = onlineInfo.player();
            double currency = DefaultEconomyProvider.INSTANCE.getCurrency(playerEntity);
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
                            if (useDecimal) {
                                currency = coinsNbt.getDouble("ValueTotalInCurrency");
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
            String valueStr = useDecimal
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
                        .requires(s -> s.hasPermission(4))
                        .then(Commands.argument("players", EntityArgument.players())
                                .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0))
                                        .executes(arguments -> addCurrency(arguments)))))

                .then(Commands.literal("set")
                        .requires(s -> s.hasPermission(4))
                        .then(Commands.argument("players", EntityArgument.players())
                                .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0))
                                        .executes(arguments -> setCurrency(arguments)))))

                .then(Commands.literal("remove")
                        .requires(s -> s.hasPermission(4))
                        .then(Commands.argument("players", EntityArgument.players())
                                .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0))
                                        .executes(arguments -> removeCurrency(arguments)))))

                .then(Commands.literal("get")
                        .requires(s -> s.hasPermission(0))
                        .executes(arguments -> getCurrency(arguments, null))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(arguments -> {
                                    Entity player = new Object() {
                                        public Entity getEntity() {
                                            try {
                                                return EntityArgument.getEntity(arguments, "player");
                                            } catch (CommandSyntaxException e) {
                                                return null;
                                            }
                                        }
                                    }.getEntity();

                                    return getCurrency(arguments, player);
                                })))

                .then(Commands.literal("pay")
                        .requires(s -> s.hasPermission(0))
                        .then(Commands.argument("target", EntityArgument.player())
                                .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.01))
                                        .executes(arguments -> payCurrency(arguments)))))

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