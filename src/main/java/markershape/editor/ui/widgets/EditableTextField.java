package markershape.editor.ui.widgets;

import markershape.editor.ui.UIResources;

import static org.lwjgl.glfw.GLFW.*;

/**
 * An inline text field that can be activated for keyboard editing,
 * validated against a type (float or hex color), and confirmed or
 * cancelled.  Renders a blinking cursor while in edit mode.
 */
public class EditableTextField {
    /** The allowed input types for validation. */
    public enum ValueType { FLOAT, HEX_COLOR }

    private String text;
    private String oldText;
    private final StringBuilder editBuffer = new StringBuilder();
    private boolean editing;
    private float x, y, scale;
    private final ValueType type;
    private float min, max;
    private java.util.function.Consumer<String> onConfirm;
    private long editStart;

    /**
     * @param initialText the initial text value
     * @param type        the validation type for the input
     * @param min         minimum allowed value (used for FLOAT)
     * @param max         maximum allowed value (used for FLOAT)
     */
    public EditableTextField(String initialText, ValueType type, float min, float max) {
        this.text = initialText;
        this.type = type;
        this.min = min;
        this.max = max;
        this.scale = 2f;
    }

    /** Registers the callback fired with the new text when editing is confirmed. */
    public void setOnConfirm(java.util.function.Consumer<String> onConfirm) {
        this.onConfirm = onConfirm;
    }

    /** Sets the screen position of the field. */
    public void setPosition(float x, float y) { this.x = x; this.y = y; }
    /** Sets the text draw scale. */
    public void setScale(float s) { this.scale = s; }
    /** Sets the displayed text. */
    public void setText(String t) { this.text = t; }
    /** Returns the current (confirmed) text. */
    public String getText() { return text; }
    /** Returns whether the field is currently in edit mode. */
    public boolean isEditing() { return editing; }

    /** Sets the validation bounds used for the FLOAT type. */
    public void setBounds(float min, float max) { this.min = min; this.max = max; }

    /** Enters edit mode with an empty buffer, remembering the previous text for cancel. */
    public void activate() {
        if (!editing) {
            editing = true;
            oldText = text;
            editBuffer.setLength(0);
            editStart = System.currentTimeMillis();
        }
    }

    /** Cancels editing and restores the text from before activation. */
    public void cancelEditing() {
        if (!editing) return;
        text = oldText;
        editing = false;
    }

    /** Draws the field text, appending a blinking cursor while editing. */
    public void render(UIResources res) {
        float[] tc = res.textColor();
        if (editing) {
            String display = editBuffer.toString();
            long elapsed = System.currentTimeMillis() - editStart;
            if ((elapsed / 500) % 2 == 0) display += "|";
            res.drawText(display, x, y, scale, tc[0], tc[1], tc[2]);
        } else {
            res.drawText(text, x, y, scale, tc[0], tc[1], tc[2]);
        }
    }

    /** Activates editing when clicked inside the field; confirms when clicked outside while editing. */
    public boolean click(UIResources res, float mx, float my) {
        String display = editing ? editBuffer.toString() : text;
        float[] ext = res.getTextExtent(display, scale);
        if (mx >= x && mx <= x + ext[0] && my >= y && my <= y + ext[1]) {
            if (!editing) {
                editing = true;
                oldText = text;
                editBuffer.setLength(0);
                editStart = System.currentTimeMillis();
            }
            return true;
        }
        if (editing) {
            confirm();
        }
        return false;
    }

    /** Appends a typed character to the edit buffer while editing. */
    public void keyChar(int codepoint) {
        if (!editing) return;
        editBuffer.append((char) codepoint);
        editStart = System.currentTimeMillis();
    }

    /** Handles Enter (confirm), Escape (cancel), and Backspace while editing. */
    public boolean keyAction(int key, int action) {
        if (!editing || action != GLFW_PRESS) return false;
        if (key == GLFW_KEY_ENTER) {
            confirm();
            return true;
        }
        if (key == GLFW_KEY_ESCAPE) {
            cancelEditing();
            return true;
        }
        if (key == GLFW_KEY_BACKSPACE && editBuffer.length() > 0) {
            editBuffer.deleteCharAt(editBuffer.length() - 1);
            editStart = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    /** Applies the edited text if valid, otherwise reverts to the previous value, and exits edit mode. */
    private void confirm() {
        String candidate = editBuffer.toString();
        if (isValid(candidate)) {
            text = candidate;
            if (onConfirm != null) onConfirm.accept(text);
        } else {
            text = oldText;
        }
        editing = false;
    }

    /** Returns whether the given string matches the field's value type and bounds. */
    private boolean isValid(String s) {
        return switch (type) {
            case FLOAT -> {
                try {
                    float v = Float.parseFloat(s);
                    yield v >= min && v <= max;
                } catch (NumberFormatException e) {
                    yield false;
                }
            }
            case HEX_COLOR -> s.matches("#[0-9A-Fa-f]{6}");
        };
    }
}
