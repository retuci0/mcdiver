package me.retucio.diver.screen.widgets.impl;

import me.retucio.diver.util.CommandUtil;
import me.retucio.diver.util.ParseUtil;
import me.retucio.diver.util.PathUtil;
import me.retucio.diver.util.ReflectionUtil;
import me.retucio.diver.screen.DiverScreen;
import me.retucio.diver.screen.widgets.AutocompleteWidget;
import me.retucio.diver.screen.widgets.TextFieldWidget;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.lwjgl.glfw.GLFW;

import java.util.*;


public class DiverFieldWidget extends TextFieldWidget {

    public final AutocompleteWidget autocomplete;

    private final Stack<String> history = new Stack<>();
    private int historyCursor = 0;

    private int mx, my;

    public DiverFieldWidget(int x, int y, int w, int maxChars) {
        super(x, y, w, "take a dive into the Minecraft instance...", maxChars);
        autocomplete = new AutocompleteWidget(x, y + h + PADDING, w, (DiverScreen.HEIGHT - 32) / AutocompleteWidget.ITEM_H);
    }


    @Override
    public void render(GuiGraphicsExtractor gui, int mx, int my) {
        super.render(gui, mx, my);

        this.y = DiverScreen.getInstance().text.getY() + DiverScreen.getInstance().text.getH() + PADDING;

        autocomplete.setX(x);
        autocomplete.setY(y + h + 2 * PADDING);
        autocomplete.setW(w);
        autocomplete.render(gui, mx, my);

        this.mx = mx;
        this.my = my;
    }

    @Override
    public void onKey(int key, int action) {
        if (action == GLFW.GLFW_RELEASE) return;

        if (key == GLFW.GLFW_KEY_TAB && autocomplete.isVisible()) {
            applyCompletion(autocomplete.getSelected());
            return;
        }

        if (key == GLFW.GLFW_KEY_ESCAPE) {
            if (autocomplete.isVisible()) {
                autocomplete.hide();
                return;
            }
        }

        if (autocomplete.keyPressed(key, action)) return;

        if (key == GLFW.GLFW_KEY_ENTER) {
            autocomplete.hide();
            String input = text.trim();
            if (!input.isBlank()) {
                history.push(input);
                historyCursor = -1;
            }

            String result = CommandUtil.evaluate(input);
            DiverScreen.getInstance().text.setText(result);
            return;
        }

        if (key == GLFW.GLFW_KEY_UP && !autocomplete.isVisible()) {
            if (history.isEmpty()) return;
            if (historyCursor == -1) historyCursor = history.size() - 1;
            else historyCursor = Math.max(0, historyCursor - 1);
            text = history.get(historyCursor);
            return;
        }

        if (key == GLFW.GLFW_KEY_DOWN && !autocomplete.isVisible()) {
            if (historyCursor == -1) return;
            historyCursor++;
            if (historyCursor >= history.size()) {
                historyCursor = -1;
                text = "";
            } else {
                text = history.get(historyCursor);
            }
            return;
        }

        super.onKey(key, action);
        refreshSuggestions();
    }

    @Override
    public void onScroll(double delta) {
        if (delta == 0 || !autocomplete.isHovered(mx, my)) return;
        autocomplete.keyPressed(
                delta > 0
                        ? GLFW.GLFW_KEY_UP
                        : GLFW.GLFW_KEY_DOWN,
                GLFW.GLFW_PRESS
        );
        super.onScroll(delta);
    }

    private void refreshSuggestions() {
        if (!text.contains(".")) {
            autocomplete.hide();
            return;
        }

        int lastDot = ParseUtil.lastDotOutsideParens(text);
        if (lastDot < 0) {
            autocomplete.hide();
            return;
        }

        String basePath = text.substring(0, lastDot);
        String prefix = text.substring(lastDot + 1);

        Object base;
        try {
            base = PathUtil.resolveToObject(basePath);
        } catch (Exception e) {
            autocomplete.hide();
            return;
        }
        if (base == null) {
            autocomplete.hide();
            return;
        }

        List<String> matches = new ArrayList<>();
        ReflectionUtil.collectMembers(base.getClass(), prefix, matches);
        autocomplete.setSuggestions(matches);
    }

    private void applyCompletion(String suggestion) {
        if (suggestion == null) return;
        int lastDot = ParseUtil.lastDotOutsideParens(text);
        if (lastDot < 0) return;

        String insert = suggestion.contains("(")
                ? suggestion.substring(0, suggestion.indexOf('(') + 1)
                : suggestion;

        text = text.substring(0, lastDot + 1) + insert;
        autocomplete.hide();
    }
}