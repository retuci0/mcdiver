package me.retucio.diver.screen.widgets;

import me.retucio.diver.screen.Widget;
import me.retucio.diver.util.DrawUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class AutocompleteWidget extends Widget {

    public static final int ITEM_H = 13;
    private final int MAX_VISIBLE;

    private List<String> suggestions = new ArrayList<>();
    private int selectedIndex = -1;
    private boolean visible = false;

    private final Color BG_COLOR = new Color(20,  20,  20,  220);
    private final Color SELECTED_COLOR = new Color(40,  80, 140,  220);
    private final Color OUTLINE_COLOR = new Color(10, 10,  10,  200);

    public AutocompleteWidget(int x, int y, int w, int maxVisible) {
        super(x, y, w, 0);
        this.MAX_VISIBLE = maxVisible;
    }

    @Override
    public void render(GuiGraphicsExtractor gui, int mx, int my) {
        if (!visible || suggestions.isEmpty()) return;

        int count = Math.min(suggestions.size(), MAX_VISIBLE);
        int totalH = count * ITEM_H + 2 * PADDING;

        DrawUtil.drawRectOutline(x - PADDING, y - PADDING, w + 2 * PADDING, totalH + 2 * PADDING, PADDING, OUTLINE_COLOR.getRGB());

        for (int i = 0; i < count; i++) {
            int itemY = y + i * ITEM_H;
            int bg = (i == selectedIndex) ? SELECTED_COLOR.getRGB() : BG_COLOR.getRGB();
            gui.fill(x, itemY, x + w, itemY + ITEM_H, bg);
            gui.text(mc.font, suggestions.get(i), x + PADDING, itemY + PADDING, -1);
        }
    }

    // not onKey
    public boolean keyPressed(int key, int action) {
        if (!visible || suggestions.isEmpty() || action == GLFW.GLFW_RELEASE) return false;
        if (key == GLFW.GLFW_KEY_UP) {
            selectedIndex = Math.max(0, selectedIndex - 1);
            return true;
        }
        if (key == GLFW.GLFW_KEY_DOWN) {
            selectedIndex = Math.min(suggestions.size() - 1, selectedIndex + 1);
            return true;
        }
        return false;
    }

    public void setSuggestions(List<String> next) {
        suggestions = new ArrayList<>(next);
        selectedIndex = -1;
        visible = !next.isEmpty();
    }

    public void hide() {
        visible = false;
        suggestions.clear();
    }

    public String getSelected() {
        if (!visible || suggestions.isEmpty() || selectedIndex == -1) return null;
        return suggestions.get(selectedIndex);
    }

    public boolean isVisible() { return visible && !suggestions.isEmpty(); }
}