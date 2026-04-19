package me.retucio.diver.screen.widgets;

import me.retucio.diver.screen.Widget;
import me.retucio.diver.util.DrawUtil;
import me.retucio.diver.util.KeyUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.jspecify.annotations.NonNull;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;


public class TextFieldWidget extends Widget {

    protected String text;
    protected final String showcaseText;
    protected int textColor;
    protected final int MAX_CHARS;

    protected final Color BG_COLOR = new Color(20, 20, 20, 200);

    public TextFieldWidget(int x, int y, int w, @NonNull String showcaseText, int maxChars) {
        super(x, y, w, 13);
        this.MAX_CHARS = maxChars;
        this.showcaseText = showcaseText;
        this.text = showcaseText;

        if (showcaseText.isEmpty()) {
            textColor = Color.WHITE.getRGB();
        } else {
            textColor = Color.LIGHT_GRAY.getRGB();
        }
    }

    @Override
    public void render(GuiGraphicsExtractor gui, int mx, int my) {
        if (text.length() > MAX_CHARS) text = text.substring(0, MAX_CHARS);

        DrawUtil.drawRectOutline(x - PADDING, y - PADDING, w + 2 * PADDING, h + 2 * PADDING, PADDING, -1);
        gui.fill(x, y, x + w, y + h, isHovered(mx, my) ? BG_COLOR.brighter().getRGB() : BG_COLOR.getRGB());
        gui.text(mc.font, text, x + PADDING, y + PADDING, textColor);
    }

    @Override
    public void onClick(int button, int action, int mx, int my) {
        if (textColor == Color.LIGHT_GRAY.getRGB()) {
            text = "";
            textColor = -1;  // white
        }
    }

    @Override
    public void onKey(int key, int action) {
        if (action == GLFW.GLFW_RELEASE) return;

        if (key == GLFW.GLFW_KEY_V && KeyUtil.isCtrlDown()) {
            text += mc.keyboardHandler.getClipboard();
            return;
        }

        switch (key) {
            case GLFW.GLFW_KEY_BACKSPACE -> {
                if (!text.isEmpty()) {
                    text = text.substring(0, text.length() - 1);
                }
            }
            case GLFW.GLFW_KEY_ESCAPE -> {
                text = showcaseText;
                textColor = Color.LIGHT_GRAY.getRGB();
            }
            default -> {
                Character c = KeyUtil.keyToChar(key);
                if (c != null) text += c;
            }
        }
    }
}
