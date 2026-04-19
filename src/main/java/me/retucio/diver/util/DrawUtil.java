package me.retucio.diver.util;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public class DrawUtil {

    private static GuiGraphicsExtractor gui;

    public static void setGuiGraphics(GuiGraphicsExtractor g) {
        gui = g;
    }

    public static void drawRectOutline(int x, int y, int width, int height, int border, int color) {
        gui.fill(x, y, x + width, y + border, color);
        gui.fill(x, y + height - border, x + width, y + height, color);
        gui.fill(x, y + border, x + border, y + height - border, color);
        gui.fill(x + width - border, y + border, x + width, y + height - border, color);
    }
}
