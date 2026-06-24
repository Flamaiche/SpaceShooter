package markershape.shape.render;

import org.lwjgl.BufferUtils;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class GridRenderer {
    private int gridVao = -1, gridVbo = -1;
    private int gridPlaneVerts;
    private boolean showAxisX = true, showAxisY = true, showAxisZ = true;
    private boolean gridVisible = true;
    private float gridStep = 1.0f;

    public void setGridVisible(boolean v) { gridVisible = v; }
    public boolean isGridVisible() { return gridVisible; }

    public void setShowAxisX(boolean v) { showAxisX = v; }
    public void setShowAxisY(boolean v) { showAxisY = v; }
    public void setShowAxisZ(boolean v) { showAxisZ = v; }
    public boolean isShowAxisX() { return showAxisX; }
    public boolean isShowAxisY() { return showAxisY; }
    public boolean isShowAxisZ() { return showAxisZ; }
    public boolean anyVisible() { return showAxisX || showAxisY || showAxisZ; }

    public void setGridStep(float step) {
        if (step <= 0f) step = 0.1f;
        gridStep = step;
        rebuild();
    }

    public void rebuild() {
        if (gridVao >= 0) { glDeleteVertexArrays(gridVao); gridVao = -1; }
        if (gridVbo >= 0) { glDeleteBuffers(gridVbo); gridVbo = -1; }
        gridPlaneVerts = 0;
        buildGridVao();
    }

    private void buildGridVao() {
        final float targetHalfSize = 10f;
        final float step = gridStep;
        int stepsPerSide = (int)(targetHalfSize / step);
        final float halfSize = stepsPerSide * step;

        ArrayList<Float> verts = new ArrayList<>();

        // plane YZ at X=0 (shown with Axe Z — contains Z axis)
        for (int i = -stepsPerSide; i <= stepsPerSide; i++) {
            float z = i * step;
            verts.add(0f); verts.add(-halfSize); verts.add(z);
            verts.add(0.2f); verts.add(0.2f); verts.add(0.3f);
            verts.add(0f); verts.add(halfSize); verts.add(z);
            verts.add(0.2f); verts.add(0.2f); verts.add(0.3f);
        }
        for (int i = -stepsPerSide; i <= stepsPerSide; i++) {
            float y = i * step;
            verts.add(0f); verts.add(y); verts.add(-halfSize);
            verts.add(0.2f); verts.add(0.2f); verts.add(0.3f);
            verts.add(0f); verts.add(y); verts.add(halfSize);
            verts.add(0.2f); verts.add(0.2f); verts.add(0.3f);
        }
        // plane XZ at Y=0 (shown with Axe X — contains X axis)
        for (int i = -stepsPerSide; i <= stepsPerSide; i++) {
            float z = i * step;
            verts.add(-halfSize); verts.add(0f); verts.add(z);
            verts.add(0.2f); verts.add(0.2f); verts.add(0.3f);
            verts.add(halfSize); verts.add(0f); verts.add(z);
            verts.add(0.2f); verts.add(0.2f); verts.add(0.3f);
        }
        for (int i = -stepsPerSide; i <= stepsPerSide; i++) {
            float x = i * step;
            verts.add(x); verts.add(0f); verts.add(-halfSize);
            verts.add(0.2f); verts.add(0.2f); verts.add(0.3f);
            verts.add(x); verts.add(0f); verts.add(halfSize);
            verts.add(0.2f); verts.add(0.2f); verts.add(0.3f);
        }
        // plane XY at Z=0 (shown with Axe Y — contains Y axis)
        for (int i = -stepsPerSide; i <= stepsPerSide; i++) {
            float y = i * step;
            verts.add(-halfSize); verts.add(y); verts.add(0f);
            verts.add(0.2f); verts.add(0.2f); verts.add(0.3f);
            verts.add(halfSize); verts.add(y); verts.add(0f);
            verts.add(0.2f); verts.add(0.2f); verts.add(0.3f);
        }
        for (int i = -stepsPerSide; i <= stepsPerSide; i++) {
            float x = i * step;
            verts.add(x); verts.add(-halfSize); verts.add(0f);
            verts.add(0.2f); verts.add(0.2f); verts.add(0.3f);
            verts.add(x); verts.add(halfSize); verts.add(0f);
            verts.add(0.2f); verts.add(0.2f); verts.add(0.3f);
        }

        gridPlaneVerts = verts.size() / 6 / 3;

        // axis lines
        verts.add(-halfSize); verts.add(0f); verts.add(0f);
        verts.add(1f); verts.add(0.2f); verts.add(0.2f);
        verts.add(halfSize); verts.add(0f); verts.add(0f);
        verts.add(1f); verts.add(0.2f); verts.add(0.2f);

        verts.add(0f); verts.add(-halfSize); verts.add(0f);
        verts.add(0.2f); verts.add(1f); verts.add(0.2f);
        verts.add(0f); verts.add(halfSize); verts.add(0f);
        verts.add(0.2f); verts.add(1f); verts.add(0.2f);

        verts.add(0f); verts.add(0f); verts.add(-halfSize);
        verts.add(0.2f); verts.add(0.2f); verts.add(1f);
        verts.add(0f); verts.add(0f); verts.add(halfSize);
        verts.add(0.2f); verts.add(0.2f); verts.add(1f);

        FloatBuffer buf = BufferUtils.createFloatBuffer(verts.size());
        for (float v : verts) buf.put(v);
        buf.flip();

        gridVao = glGenVertexArrays();
        gridVbo = glGenBuffers();
        glBindVertexArray(gridVao);
        glBindBuffer(GL_ARRAY_BUFFER, gridVbo);
        glBufferData(GL_ARRAY_BUFFER, buf, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 6 * 4, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 6 * 4, 3 * 4);
        glEnableVertexAttribArray(1);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public void render() {
        if (gridVao < 0) buildGridVao();
        if (gridVao < 0) return;
        if (!gridVisible || (!showAxisX && !showAxisY && !showAxisZ)) return;

        int pv = gridPlaneVerts;
        glDepthMask(false);
        glBindVertexArray(gridVao);

        // pass 1: grid planes (dim, thin)
        glLineWidth(1f);
        if (showAxisX) glDrawArrays(GL_LINES, pv, pv);      // XZ plane
        if (showAxisY) glDrawArrays(GL_LINES, 2 * pv, pv);  // XY plane
        if (showAxisZ) glDrawArrays(GL_LINES, 0, pv);       // YZ plane

        // pass 2: axis lines (bright, thick)
        glLineWidth(3f);
        if (showAxisX) glDrawArrays(GL_LINES, 3 * pv, 2);
        if (showAxisY) glDrawArrays(GL_LINES, 3 * pv + 2, 2);
        if (showAxisZ) glDrawArrays(GL_LINES, 3 * pv + 4, 2);

        glLineWidth(1f);
        glBindVertexArray(0);
        glDepthMask(true);
    }

    public void cleanup() {
        if (gridVao >= 0) { glDeleteVertexArrays(gridVao); gridVao = -1; }
        if (gridVbo >= 0) { glDeleteBuffers(gridVbo); gridVbo = -1; }
        gridPlaneVerts = 0;
    }
}
