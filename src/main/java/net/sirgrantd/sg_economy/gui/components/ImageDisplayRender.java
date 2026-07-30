package net.sirgrantd.sg_economy.gui.components;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ImageDisplayRender {
    
    public static final ResourceLocation DISPLAY_VIEW_DEFAULT = ResourceLocation.parse("sg_economy:textures/gui/display_view_default.png");
    public static final ResourceLocation DISPLAY_VIEW_MAGIC_COINS = ResourceLocation.parse("sg_economy:textures/gui/display_view_magic_coins.png");

    private final AbstractContainerScreen<?> parentGui;
    private final int xOffset;
    private final int yOffset;
    private final ResourceLocation texture;

    public ImageDisplayRender(AbstractContainerScreen<?> parentGui, int xOffset, int yOffset, ResourceLocation texture) {
        this.parentGui = parentGui;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.texture = texture;
    }

    public void renderWidget(GuiGraphics guiGraphics, int sizeX, int sizeY) {
        
        int x = parentGui.getGuiLeft() + xOffset;
        int y = parentGui.getGuiTop() + yOffset;

        if(parentGui instanceof CreativeModeInventoryScreen creativeScreen) {
            boolean isInventoryTab = creativeScreen.isInventoryOpen();

            if (!isInventoryTab) {
                return;
            }
        }
    
        guiGraphics.blit(texture, x, y, 0, 0, sizeX, sizeY, sizeX, sizeY);
    }
}
