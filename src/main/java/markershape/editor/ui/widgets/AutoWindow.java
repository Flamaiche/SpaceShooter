package markershape.editor.ui.widgets;

import markershape.config.ConfigParametres;
import markershape.editor.ui.UIElement;
import markershape.editor.ui.UIResources;
import markershape.editor.ui.menu.BlurBackground;

import java.util.ArrayList;
import java.util.List;

/**
 * A floating window that auto-sizes its width and height to fit its content.
 * Content is a vertical sequence of rows. Each row is either:
 * <ul>
 *   <li>A <b>text</b> line that wraps at maxWidth (measured by
 *       {@link UIResources#getTextExtent}), or</li>
 *   <li>A <b>child row</b> made of one or more {@link UIElement}s laid
 *       out horizontally.</li>
 * </ul>
 * Call {@link #autoSize()} after adding content to compute the final
 * dimensions. Call {@link #centerOn} afterwards to position.
 */
public class AutoWindow extends UIElement {

    /* ---------- constants ------------------------------------------------ */
    private static final float PAD_X     = 14f;
    private static final float PAD_Y     = 12f;
    private static final float ROW_GAP   = 6f;
    private static final float LINE_H    = 18f;
    private static final float CELL_GAP  = 12f;

    /* ---------- rows ----------------------------------------------------- */
    private sealed interface Row permits TextRow, ChildRow { }
    private record TextRow(String text, float scale, List<String> wrapped) implements Row { }
    private record ChildRow(List<UIElement> children) implements Row { }

    private final List<Row> rows = new ArrayList<>();

    /* ---------- window state --------------------------------------------- */
    private float maxW  = 640f;
    private String title = null;
    private float titleScale = 1.6f;
    public boolean showBackground = true;

    /* -------------------------------------------------------------------- */
    /** @param res the shared UI resources used to render the window */
    public AutoWindow(UIResources res) { super(res); }

    /** Set the maximum pixel width before text lines wrap. */
    public AutoWindow setMaxWidth(float v) { this.maxW = v; return this; }

    /** Optional title drawn above all rows. */
    public AutoWindow setTitle(String t) { this.title = t; return this; }

    /** Append a text row (default scale 1.2). Wraps when wider than maxW. */
    public AutoWindow addText(String text) { return addText(text, 1.2f); }

    /** Append a text row at the given scale. Wraps when wider than maxW. */
    public AutoWindow addText(String text, float scale) {
        rows.add(new TextRow(text, scale, wrap(text, scale)));
        return this;
    }

    /** Append a row containing one or more horizontal child UI elements. */
    public AutoWindow addChildRow(UIElement... children) {
        List<UIElement> list = new ArrayList<>();
        for (UIElement c : children) list.add(c);
        rows.add(new ChildRow(list));
        return this;
    }

    /** Remove all rows and reset title. */
    public AutoWindow clear() {
        rows.clear();
        title = null;
        return this;
    }

    /* ---------- measurement ---------------------------------------------- */

    /**
     * Measures all rows and sets {@code w} / {@code h} on this element.
     * Also positions child elements relative to this window's origin so they
     * can be rendered and hit-tested correctly.
     */
    public void autoSize() {
        float contentW = 0f;
        float contentH = 0f;

        if (title != null) {
            contentW = Math.max(contentW, res.getTextExtent(title, titleScale)[0]);
            contentH += LINE_H + ROW_GAP;
        }

        for (Row row : rows) {
            switch (row) {
                case TextRow tr -> {
                    float maxLineW = 0f;
                    for (String line : tr.wrapped) {
                        maxLineW = Math.max(maxLineW, res.getTextExtent(line, tr.scale)[0]);
                    }
                    contentW = Math.max(contentW, maxLineW);
                    contentH += tr.wrapped.size() * LINE_H + ROW_GAP;
                }
                case ChildRow cr -> {
                    float rowW = 0f;
                    float rowH = 0f;
                    for (UIElement c : cr.children) {
                        rowW += c.w;
                        rowH = Math.max(rowH, c.h);
                        rowW += CELL_GAP;
                    }
                    if (!cr.children.isEmpty()) rowW -= CELL_GAP;
                    contentW = Math.max(contentW, rowW);
                    contentH += rowH + ROW_GAP;
                }
            }
        }

        w = contentW + 2 * PAD_X;
        h = contentH + 2 * PAD_Y;
    }

    /** Centre this window on the given screen coordinates. */
    public void centerOn(float cx, float cy) {
        x = cx - w / 2f;
        y = cy - h / 2f;
        repositionChildren();
    }

    /* ---------- rendering ------------------------------------------------ */

    /** Draws the background, title, text rows, and child rows of the window. */
    @Override
    public void render() {
        if (!visible) return;
        res.begin2D();

        float[] t = textColor();

        if (showBackground) {
            float[] c = res.menuColor();
            res.drawQuad(x, y, w, h, c[0], c[1], c[2], BlurBackground.panelAlpha());
        }

        float cy = y + PAD_Y;

        if (title != null) {
            res.drawText(title, x + PAD_X, cy, titleScale, t[0], t[1], t[2]);
            cy += LINE_H + ROW_GAP;
        }

        for (Row row : rows) {
            switch (row) {
                case TextRow tr -> {
                    for (String line : tr.wrapped) {
                        res.drawText(line, x + PAD_X, cy, tr.scale, t[0], t[1], t[2]);
                        cy += LINE_H;
                    }
                    cy += ROW_GAP;
                }
                case ChildRow cr -> {
                    float cx = x + PAD_X;
                    float maxH = 0f;
                    for (UIElement child : cr.children) {
                        child.x = cx;
                        child.y = cy;
                        maxH = Math.max(maxH, child.h);
                        cx += child.w + CELL_GAP;
                    }
                    for (UIElement child : cr.children) child.render();
                    cy += maxH + ROW_GAP;
                }
            }
        }
    }

    /* ---------- layout helpers ------------------------------------------- */

    /** Re-positions the child elements of each row relative to the window's current origin. */
    private void repositionChildren() {
        float cy = y + PAD_Y;
        if (title != null) cy += LINE_H + ROW_GAP;
        for (Row row : rows) {
            if (row instanceof ChildRow cr) {
                float cx = x + PAD_X;
                float maxH = 0f;
                for (UIElement child : cr.children) {
                    child.x = cx;
                    child.y = cy;
                    maxH = Math.max(maxH, child.h);
                    cx += child.w + CELL_GAP;
                }
                cy += maxH + ROW_GAP;
            } else if (row instanceof TextRow tr) {
                cy += tr.wrapped.size() * LINE_H + ROW_GAP;
            }
        }
    }

    /* ---------- text wrapping -------------------------------------------- */

    /** Splits the given text into lines that fit within the maximum width, wrapping on word boundaries. */
    private List<String> wrap(String text, float scale) {
        float maxTextW = maxW - 2 * PAD_X;
        if (maxTextW <= 0) maxTextW = 600f;

        float totalW = res.getTextExtent(text, scale)[0];
        if (totalW <= maxTextW) {
            List<String> one = new ArrayList<>();
            one.add(text);
            return one;
        }

        String[] words = text.split("\\s+");
        List<String> lines = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (String word : words) {
            String test = current.isEmpty() ? word : current + " " + word;
            float tw = res.getTextExtent(test, scale)[0];
            if (tw > maxTextW && !current.isEmpty()) {
                lines.add(current.toString());
                current = new StringBuilder(word);
            } else {
                current = current.isEmpty() ? new StringBuilder(word) : current.append(' ').append(word);
            }
        }
        if (!current.isEmpty()) lines.add(current.toString());
        return lines;
    }

    /** Returns the configured text color as [R, G, B] normalized to 0-1. */
    private float[] textColor() {
        ConfigParametres cfg = ConfigParametres.get();
        return new float[]{
            cfg.getFloat("textR") / 255f,
            cfg.getFloat("textG") / 255f,
            cfg.getFloat("textB") / 255f
        };
    }
}
