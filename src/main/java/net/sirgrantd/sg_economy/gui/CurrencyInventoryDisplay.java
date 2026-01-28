package net.sirgrantd.sg_economy.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

import net.sirgrantd.sg_economy.config.ClientConfig;
import net.sirgrantd.sg_economy.gui.components.ImageDisplayRender;
import net.sirgrantd.sg_economy.gui.components.TextDisplayRender;
import net.sirgrantd.sg_economy.internal.DefaultEconomyProvider;

@EventBusSubscriber({ Dist.CLIENT })
public class CurrencyInventoryDisplay {
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void eventHandler(ScreenEvent.Render.Post event) {
        Screen screen = event.getScreen();

        boolean isInventory = screen instanceof InventoryScreen;
        boolean isCreative = screen instanceof CreativeModeInventoryScreen;
        boolean isCurios = false;

        if (ModList.get().isLoaded("curios")) {
            try {
                Class<?> curiosScreenClass = Class.forName("top.theillusivec4.curios.api.client.ICuriosScreen");
                isCurios = curiosScreenClass.isInstance(screen);
            } catch (ClassNotFoundException ignored) {
            }
        }

        if (!ClientConfig.activeDisplayCurrency) {
            return;
        }

        if (isInventory || isCreative || isCurios) {

            AbstractContainerScreen<?> gui = (AbstractContainerScreen<?>) screen;
            isCreative = screen instanceof CreativeModeInventoryScreen;

            Player player = Minecraft.getInstance().player;

            String amoutCurrency = "";
            if (DefaultEconomyProvider.INSTANCE.isDecimalCurrency()) {
                double balance = DefaultEconomyProvider.INSTANCE.getCurrency(player);
                amoutCurrency = String.format("%.2f", balance);
            } else {
                int balance = DefaultEconomyProvider.INSTANCE.getCoins(player);
                amoutCurrency = Integer.toString(balance);
            }

            int xOffsetImage = isCreative ? 0 + ClientConfig.xDisplayCurrency : 0 + ClientConfig.xDisplayCurrency;
            int yOffsetImage = isCreative ? -75 + ClientConfig.yDisplayCurrency : -28 + ClientConfig.yDisplayCurrency;

            int xOffsetText = 90 + ClientConfig.xDisplayCurrency;
            int yOffsetText = isCreative ? -67 + ClientConfig.yDisplayCurrency : -20 + ClientConfig.yDisplayCurrency;
            
            ResourceLocation DisplayImage = ClientConfig.typeDisplayCurrency.equals("magic_coins") ?
                ImageDisplayRender.DISPLAY_VIEW_MAGIC_COINS : ImageDisplayRender.DISPLAY_VIEW_DEFAULT;
            
            ImageDisplayRender displayRender = new ImageDisplayRender(gui, xOffsetImage, yOffsetImage, DisplayImage);
            displayRender.renderWidget(event.getGuiGraphics(), 96, 23);

            TextDisplayRender textRender = new TextDisplayRender(gui, xOffsetText, yOffsetText, amoutCurrency);
            textRender.renderText(event.getGuiGraphics());
        }
    }
}