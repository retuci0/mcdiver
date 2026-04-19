package me.retucio.diver.screen.widgets;

import me.retucio.diver.screen.Widget;
import me.retucio.diver.util.DrawUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class TextWidget extends Widget {

    protected String text;
    protected final String defaultText;

    protected final Color BG_COLOR = new Color(20, 20, 20, 200);

    public TextWidget(int x, int y, int w, int lines, String defaultText) {
        super(x, y, w, lines * 13);
        this.text = this.defaultText = defaultText;
    }

    @Override
    public void render(GuiGraphicsExtractor gui, int mx, int my) {
        List<String> lines = getWrappedLines();
        int renderedHeight = lines.size() * 13 + 2 * PADDING;

        DrawUtil.drawRectOutline(x - PADDING, y - PADDING, w + 2 * PADDING, renderedHeight, PADDING, -1);
        gui.fill(x, y, x + w, y + renderedHeight - 2 * PADDING, isHovered(mx, my) ? BG_COLOR.brighter().getRGB() : BG_COLOR.getRGB());

        int lineY = y + PADDING;
        for (String line : lines) {
            gui.text(mc.font, line, x + PADDING, lineY, -1);
            lineY += 13;
        }
    }

    protected List<String> getWrappedLines() {
        List<String> lines = new ArrayList<>();
        int maxWidth = w - 2 * PADDING;

        for (String paragraph : text.split("\n", -1)) {
            if (paragraph.isEmpty()) {
                lines.add("");
                continue;
            }
            String[] words = paragraph.split(" ");
            StringBuilder current = new StringBuilder();

            for (String word : words) {
                String candidate = current.isEmpty() ? word : current + " " + word;
                if (mc.font.width(candidate) <= maxWidth) {
                    current = new StringBuilder(candidate);
                } else {
                    if (current.isEmpty()) {
                        lines.add(word);
                    } else {
                        lines.add(current.toString());
                        current = new StringBuilder(word);
                    }
                }
            }
            if (!current.isEmpty()) lines.add(current.toString());
        }

        return lines;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
