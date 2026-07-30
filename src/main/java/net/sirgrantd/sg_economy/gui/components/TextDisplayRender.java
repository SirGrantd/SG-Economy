package net.sirgrantd.sg_economy.gui.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TextDisplayRender {

    private final AbstractContainerScreen<?> parentGui;
    private final int rightXOffset;
    private final int yOffset;
    private final String text;

    public TextDisplayRender(AbstractContainerScreen<?> parentGui, int rightXOffset, int yOffset, String text) {
        this.parentGui = parentGui;
        this.rightXOffset = rightXOffset;
        this.yOffset = yOffset;
        this.text = text;
    }

    public void renderText(GuiGraphics guiGraphics) {
        int fontWidth = Minecraft.getInstance().font.width(text);
        int x = parentGui.getGuiLeft() + rightXOffset - fontWidth;
        int y = parentGui.getGuiTop() + yOffset;

        if (parentGui instanceof CreativeModeInventoryScreen creativeScreen) {
            boolean isInventoryTab = creativeScreen.isInventoryOpen();

            if (!isInventoryTab) {
                return;
            }
        }

        guiGraphics.drawString(
            Minecraft.getInstance().font, text, x, y, 0xFFFFFF, false
        );
    }
}