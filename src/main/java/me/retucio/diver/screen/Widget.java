package me.retucio.diver.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;


public abstract class Widget {

    protected int x, y, w, h;
    protected final int PADDING = 2;

    protected final Minecraft mc = Minecraft.getInstance();

    public Widget(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public abstract void render(GuiGraphicsExtractor gui, int mx, int my);
    public void onClick(int button, int action, int mx, int my) {}
    public void onKey(int key, int action) {}


    /* getters */

    public int getX() { return x; }
    public int getY() { return y; }
    public int getW() { return w; }
    public int getH() { return h; }


    /* setters */

    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public void setW(int w) { this.w = w; }
    public void setH(int h) { this.h = h; }


    /* utility methods */

    public boolean isHovered(int mx, int my) {
        return mx >= x && mx <= x + w
            && my >= y && my <= y + h;
    }

}
