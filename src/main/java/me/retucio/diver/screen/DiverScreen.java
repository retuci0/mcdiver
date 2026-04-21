package me.retucio.diver.screen;

import me.retucio.diver.Diver;
import me.retucio.diver.screen.widgets.impl.DiverFieldWidget;
import me.retucio.diver.screen.widgets.TextFieldWidget;
import me.retucio.diver.screen.widgets.TextWidget;
import me.retucio.diver.util.ChatUtil;
import me.retucio.diver.util.DrawUtil;
import me.retucio.diver.util.KeyUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;


public class DiverScreen extends Screen {

    private static DiverScreen INSTANCE;
    private static final Minecraft mc = Minecraft.getInstance();

    public static final int WIDTH = 600;
    public static final int HEIGHT = 300;
    public static final int PADDING = 2;

    public static final Color BG_COLOR = new Color(60, 60, 60, 120);
    public static final Color OUTLINE_COLOR = new Color(10, 10, 10, 200);

    private int x, y;

    private @Nullable Widget selected;
    private int focusedIndex = -1;
    private final List<Widget> widgets = new ArrayList<>();

    public DiverFieldWidget textField;
    public TextWidget text;

    public DiverScreen() {
        super(Component.literal(Diver.MOD_ID));
        addWidgets();
    }

    private void initWidgets() {
        int bgX = mc.getWindow().getGuiScaledWidth() / 2 - WIDTH / 2;
        int bgY = mc.getWindow().getGuiScaledHeight() / 2 - HEIGHT / 2;

        int contentX = bgX + PADDING;
        int contentW = WIDTH - 2 * PADDING;

        int textY = bgY + PADDING;
        int fieldY = textY + 16 + PADDING;

        text = new TextWidget(
                contentX,
                textY,
                contentW,
                "use the bar below to browse fields and methods"
        );

        textField = new DiverFieldWidget(
                contentX,
                fieldY,
                contentW,
                97
        );
    }

    private void addWidgets() {
        initWidgets();
        widgets.add(text);
        widgets.add(textField);
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor gui, int mx, int my, float delta) {
        super.extractRenderState(gui, mx, my, delta);

        x = mc.getWindow().getGuiScaledWidth() / 2 - WIDTH / 2;
        y = mc.getWindow().getGuiScaledHeight() / 2 - HEIGHT / 2;

        DrawUtil.drawRectOutline(
                x - PADDING,
                y - PADDING,
                WIDTH + 2 * PADDING,
                HEIGHT + 2 * PADDING, PADDING,
                OUTLINE_COLOR.getRGB()
        );

        gui.fill(x, y, x + WIDTH, y + HEIGHT, isHovered(mx, my) ? BG_COLOR.brighter().getRGB() : BG_COLOR.getRGB());

        for (Widget widget : widgets) {
            widget.render(gui, mx, my);
        }
    }


    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean doubled) {
        for (Widget widget : widgets) {
            if (widget.isHovered((int) event.x(), (int) event.y())) {
                select(widget);
                break;
            } else {
                select(null);
            }
        }

        for (Widget widget : widgets) {
            widget.onClick(event.button(), GLFW.GLFW_PRESS, (int) event.x(), (int) event.y());
        }

        return super.mouseClicked(event, doubled);
    }

    @Override
    public boolean mouseReleased(@NonNull MouseButtonEvent event) {
        for (Widget widget : widgets) {
            widget.onClick(event.button(), GLFW.GLFW_RELEASE, (int) event.x(), (int) event.y());
        }

        return super.mouseReleased(event);
    }

    @Override
    public boolean keyPressed(@NonNull KeyEvent event) {
        boolean wasAutocompleting = textField.autocomplete.isVisible();
        if (selected != null) selected.onKey(event.key(), GLFW.GLFW_PRESS);

        if (event.key() == GLFW.GLFW_KEY_TAB) {
            if (selected == textField && wasAutocompleting) {
                return super.keyPressed(event);
            }

            if (KeyUtil.isShiftDown()) --focusedIndex; else ++focusedIndex;
            focusedIndex = ((focusedIndex % widgets.size()) + widgets.size()) % widgets.size();
            select(widgets.get(focusedIndex));
            return true;
        } else if (event.key() == GLFW.GLFW_KEY_ESCAPE) {
            select(null);
        }

        return super.keyPressed(event);
    }

    @Override
    public boolean keyReleased(@NonNull KeyEvent event) {
        if (selected != null) selected.onKey(event.key(), GLFW.GLFW_RELEASE);
        if (event.key() == GLFW.GLFW_KEY_ESCAPE) select(null);
        return super.keyReleased(event);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        if (selected != null) selected.onScroll(scrollY);
        return super.mouseScrolled(x, y, scrollX, scrollY);
    }


    public boolean isHovered(int mx, int my) {
        return mx >= x && mx <= x + WIDTH
            && my >= y && my <= y + HEIGHT;
    }

    public @Nullable Widget getSelected() {
        return selected;
    }

    public void select(@Nullable Widget widget) {
        this.selected = widget;
        this.focusedIndex = widget == null ? -1 : widgets.indexOf(widget);  // sync
        if (widget != null) widget.onSelect();
    }

    public boolean isSelected(@NonNull Widget widget) {
        return this.selected == widget;
    }

    public static DiverScreen getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DiverScreen();
        }
        return INSTANCE;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return super.shouldCloseOnEsc() && selected == null;
    }
}
