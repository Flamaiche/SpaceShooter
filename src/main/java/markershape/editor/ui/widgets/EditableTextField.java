package markershape.editor.ui.widgets;

import gamegl.gestion.texte.Text;
import learngl.Shader;

import static org.lwjgl.glfw.GLFW.*;

public class EditableTextField {
    public enum ValueType { FLOAT, HEX_COLOR }

    private String text;
    private String oldText;
    private final StringBuilder editBuffer = new StringBuilder();
    private boolean editing;
    private float x, y, scale;
    private float r = 1f, g = 1f, b = 1f;
    private final ValueType type;
    private float min, max;
    private java.util.function.Consumer<String> onConfirm;
    private long editStart;

    public EditableTextField(String initialText, ValueType type, float min, float max) {
        this.text = initialText;
        this.type = type;
        this.min = min;
        this.max = max;
        this.scale = 2f;
    }

    public void setOnConfirm(java.util.function.Consumer<String> onConfirm) {
        this.onConfirm = onConfirm;
    }

    public void setPosition(float x, float y) { this.x = x; this.y = y; }
    public void setColor(float r, float g, float b) { this.r = r; this.g = g; this.b = b; }
    public void setScale(float s) { this.scale = s; }
    public void setText(String t) { this.text = t; }
    public String getText() { return text; }
    public boolean isEditing() { return editing; }

    public void setBounds(float min, float max) { this.min = min; this.max = max; }

    public void activate() {
        if (!editing) {
            editing = true;
            oldText = text;
            editBuffer.setLength(0);
            editStart = System.currentTimeMillis();
        }
    }

    public void cancelEditing() {
        if (!editing) return;
        text = oldText;
        editing = false;
    }

    public void render(Shader textShader) {
        if (editing) {
            String display = editBuffer.toString();
            long elapsed = System.currentTimeMillis() - editStart;
            if ((elapsed / 500) % 2 == 0) display += "|";
            Text.drawText(textShader, display, x, y, scale, 1f, 1f, 0f);
        } else {
            Text.drawText(textShader, text, x, y, scale, r, g, b);
        }
    }

    public boolean click(float mx, float my) {
        String display = editing ? editBuffer.toString() : text;
        float[] ext = Text.getTextExtent(display, scale);
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

    public void keyChar(int codepoint) {
        if (!editing) return;
        editBuffer.append((char) codepoint);
        editStart = System.currentTimeMillis();
    }

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
