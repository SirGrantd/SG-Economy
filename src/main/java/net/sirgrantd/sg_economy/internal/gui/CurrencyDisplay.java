package net.sirgrantd.sg_economy.internal.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.sirgrantd.celesthyd.api.gui.CelesthydImage;
import net.sirgrantd.celesthyd.api.gui.CelesthydText;
import net.sirgrantd.sg_economy.SGEconomyMod;
import net.sirgrantd.sg_economy.api.SGEconomyApi;
import net.sirgrantd.sg_economy.internal.config.ClientConfig;

@EventBusSubscriber({ Dist.CLIENT })
public class CurrencyDisplay {

    private static final Identifier DISPLAY_VIEW_DEFAULT = Identifier.fromNamespaceAndPath(SGEconomyMod.MOD_ID,
            "textures/gui/sprites/display_view_default.png");
    private static final Identifier DISPLAY_VIEW_MAGIC_COINS = Identifier.fromNamespaceAndPath(SGEconomyMod.MOD_ID,
            "textures/gui/sprites/display_view_magic_coins.png");

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void eventHandler(ScreenEvent.Render.Post event) {
        Screen screen = event.getScreen();

        boolean isInventory = screen instanceof InventoryScreen;
        boolean isCreative = screen instanceof CreativeModeInventoryScreen;
        boolean isCurios = false;
        boolean isMagicCoins = false;

        if (ModList.get().isLoaded("curios")) {
            try {
                Class<?> curiosScreenClass = Class.forName("top.theillusivec4.curios.api.client.ICuriosScreen");
                isCurios = curiosScreenClass.isInstance(screen);

            } catch (ClassNotFoundException ignored) {
            }
        }

        if (ModList.get().isLoaded("magic_coins")) {
            isMagicCoins = true;
        }

        if (!ClientConfig.activeDisplayCurrency) {
            return;
        }

        if (isInventory || isCreative || isCurios) {

            AbstractContainerScreen<?> gui = (AbstractContainerScreen<?>) screen;
            isCreative = screen instanceof CreativeModeInventoryScreen;

            Player player = Minecraft.getInstance().player;
            double balanceValue = SGEconomyApi.getBalance(player);

            String balance = SGEconomyApi.isDecimalSystem() ? String.format("%.2f", balanceValue)
                    : String.format("%d", (long) balanceValue);

            int xOffsetImage = isCreative ? 0 + ClientConfig.xDisplayCurrency : 0 + ClientConfig.xDisplayCurrency;
            int yOffsetImage = isCreative ? -56 + ClientConfig.yDisplayCurrency : -26 + ClientConfig.yDisplayCurrency;

            int displayWidth = 96;
            int displayHeight = 24;

            int fontWidth = Minecraft.getInstance().font.width(balance);

            int xOffsetText = xOffsetImage + displayWidth - fontWidth - 5;
            int yOffsetText = yOffsetImage + (displayHeight / 2) - 3;

            Identifier DisplayImage = isMagicCoins ? DISPLAY_VIEW_MAGIC_COINS
                    : DISPLAY_VIEW_DEFAULT;

            CelesthydImage displayImage = new CelesthydImage(gui, xOffsetImage, yOffsetImage, DisplayImage);
            displayImage.extractContents(event.getGuiGraphics(), displayWidth, displayHeight);

            CelesthydText displayText = new CelesthydText(gui, xOffsetText, yOffsetText, balance);
            displayText.extractContents(event.getGuiGraphics());
        }
    }
}
