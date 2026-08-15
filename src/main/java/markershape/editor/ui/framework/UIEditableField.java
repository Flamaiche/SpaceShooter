package markershape.editor.ui.framework;

import markershape.editor.ui.widgets.EditableTextField;

/** Champ de texte éditable (hexa / float), intégré à l'arbre d'UI comme n'importe quelle fenêtre. */
public class UIEditableField extends UIElement {
    public boolean centered = false;
    public float relScale = 2f / 720f;
    public float tR = 1f, tG = 1f, tB = 1f;

    private final EditableTextField impl;

    public UIEditableField(String initialText, EditableTextField.ValueType type, float min, float max) {
        this.impl = new EditableTextField(initialText, type, min, max);
    }

    public UIEditableField(String initialText, EditableTextField.ValueType type) {
        this(initialText, type, 0, 0);
    }

    public void setBounds(float min, float max) { impl.setBounds(min, max); }
    public void setOnConfirm(java.util.function.Consumer<String> onConfirm) { impl.setOnConfirm(onConfirm); }
    public void setColor(float r, float g, float b) { tR = r; tG = g; tB = b; }
    public void setText(String t) { impl.setText(t); }
    public String getText() { return impl.getText(); }
    public boolean isEditing() { return impl.isEditing(); }
    public void activate() { impl.activate(); }
    public void cancelEditing() { impl.cancelEditing(); }
    public void keyAction(int key, int action) { impl.keyAction(key, action); }
    public void keyChar(int codepoint) { impl.keyChar(codepoint); }

    public boolean click(float mx, float my, UIRenderer r) {
        return onClickSelf(mx, my, r);
    }

    private float[] drawPos(UIRenderer r) {
        float scale = relScale * r.getHeight();
        float[] ext = r.textExtent(impl.getText(), scale);
        float tx = absX(r), ty = absY(r);
        if (centered) {
            tx = absX(r) + (absW(r) - ext[0]) / 2f;
            ty = absY(r) + (absH(r) - ext[1]) / 2f;
        }
        return new float[]{tx, ty, scale};
    }

    private void sync(UIRenderer r) {
        float[] pos = drawPos(r);
        impl.setPosition(pos[0], pos[1]);
        impl.setScale(pos[2]);
    }

    @Override
    protected void renderSelf(UIRenderer r) {
        sync(r);
        impl.setColor(tR, tG, tB);
        impl.render(r.textShader());
    }

    @Override
    protected boolean onClickSelf(float px, float py, UIRenderer r) {
        sync(r);
        return impl.click(px, py);
    }
}
