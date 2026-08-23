package net.sirgrantd.sg_economy.internal.gui;

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
import net.neoforged.neoforge.client.event.ContainerScreenEvent;
import net.sirgrantd.celesthyd.api.gui.CelesthydImage;
import net.sirgrantd.celesthyd.api.gui.CelesthydText;
import net.sirgrantd.sg_economy.SGEconomyMod;
import net.sirgrantd.sg_economy.api.SGEconomyApi;
import net.sirgrantd.sg_economy.internal.config.ClientConfig;

@EventBusSubscriber({ Dist.CLIENT })
public class CurrencyDisplay {

    private static final ResourceLocation DISPLAY_VIEW_DEFAULT = ResourceLocation.fromNamespaceAndPath(SGEconomyMod.MOD_ID,
            "textures/gui/sprites/display_view_default.png");
    private static final ResourceLocation DISPLAY_VIEW_MAGIC_COINS = ResourceLocation.fromNamespaceAndPath(SGEconomyMod.MOD_ID,
            "textures/gui/sprites/display_view_magic_coins.png");

    private static double lastBalanceValue = -1.0;
    private static AbstractContainerScreen<?> lastGui = null;
    private static String cachedBalance = "";
    private static CelesthydImage cachedDisplayImage = null;
    private static CelesthydText cachedDisplayText = null;

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void eventHandler(ContainerScreenEvent.Render.Foreground event) {
        Screen screen = event.getContainerScreen();

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

            AbstractContainerScreen<?> gui = event.getContainerScreen();
            isCreative = screen instanceof CreativeModeInventoryScreen;

            Player player = Minecraft.getInstance().player;
            double balanceValue = SGEconomyApi.getBalance(player);

            if (balanceValue != lastBalanceValue || gui != lastGui) {
                lastBalanceValue = balanceValue;
                lastGui = gui;

                cachedBalance = SGEconomyApi.isDecimalSystem() ? String.format("%.2f", balanceValue)
                        : String.format("%d", (long) balanceValue);

                int cachedFontWidth = Minecraft.getInstance().font.width(cachedBalance);

                int xOffsetImage = isCreative ? 0 + ClientConfig.xDisplayCurrencyCreative
                        : 0 + ClientConfig.xDisplayCurrency;
                int yOffsetImage = isCreative ? 165 + ClientConfig.yDisplayCurrencyCreative
                        : -26 + ClientConfig.yDisplayCurrency;

                int displayWidth = 96;
                int displayHeight = 24;

                int xOffsetText = xOffsetImage + displayWidth - cachedFontWidth - 5;
                int yOffsetText = yOffsetImage + (displayHeight / 2) - 3;

                ResourceLocation DisplayImage = isMagicCoins ? DISPLAY_VIEW_MAGIC_COINS
                        : DISPLAY_VIEW_DEFAULT;

                cachedDisplayImage = new CelesthydImage(gui, xOffsetImage, yOffsetImage, DisplayImage);
                cachedDisplayText = new CelesthydText(gui, xOffsetText, yOffsetText, cachedBalance);
            }

            event.getGuiGraphics().pose().pushPose();
            event.getGuiGraphics().pose().translate(-gui.getGuiLeft(), -gui.getGuiTop(), 0);

            if (cachedDisplayImage != null && cachedDisplayText != null) {
                cachedDisplayImage.render(event.getGuiGraphics(), 96, 24);
                cachedDisplayText.render(event.getGuiGraphics());
            }

            event.getGuiGraphics().pose().popPose();
        }
    }
}
