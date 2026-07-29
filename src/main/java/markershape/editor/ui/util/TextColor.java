package markershape.editor.ui.util;

public class TextColor {

    public static float contrast(float r, float g, float b) {
        float lum = 0.299f * r + 0.587f * g + 0.114f * b;
        return lum > 0.5f ? 0f : 1f;
    }

    public static float[] composite(float[] fg, float[] bg) {
        float fgA = fg.length >= 4 ? fg[3] : 1f;
        float bgA = bg.length >= 4 ? bg[3] : 1f;
        float a = fgA + bgA * (1f - fgA);
        if (a == 0f) return new float[]{bg[0], bg[1], bg[2]};
        float r = (fg[0] * fgA + bg[0] * bgA * (1f - fgA)) / a;
        float g = (fg[1] * fgA + bg[1] * bgA * (1f - fgA)) / a;
        float b = (fg[2] * fgA + bg[2] * bgA * (1f - fgA)) / a;
        return new float[]{r, g, b};
    }

    public static float[] composite(float r1, float g1, float b1, float a1,
                                     float r2, float g2, float b2, float a2) {
        return composite(new float[]{r1, g1, b1, a1}, new float[]{r2, g2, b2, a2});
    }

    public static float[] composite(float r, float g, float b, float a, float[] bg) {
        return composite(new float[]{r, g, b, a}, bg);
    }

    public static float[] menuText(float textR, float textG, float textB,
                                    float bgR, float bgG, float bgB,
                                    float refR, float refG, float refB) {
        float tLum = 0.299f * textR + 0.587f * textG + 0.114f * textB;
        float refBgLum = 0.299f * refR + 0.587f * refG + 0.114f * refB;
        float bgLum = 0.299f * bgR + 0.587f * bgG + 0.114f * bgB;

        float refCR = (Math.max(tLum, refBgLum) + 0.05f) / (Math.min(tLum, refBgLum) + 0.05f);

        boolean textOnDark = bgLum >= 0.5f;
        float target;
        if (textOnDark) {
            target = (bgLum + 0.05f) / refCR - 0.05f;
            if (target < 0.001f) target = 0.001f;
        } else {
            target = refCR * (bgLum + 0.05f) - 0.05f;
            if (target > 0.999f) target = 0.999f;
        }

        if (target <= tLum) {
            float s = tLum > 0f ? target / tLum : 0f;
            return new float[]{textR * s, textG * s, textB * s};
        } else {
            float s = (target - tLum) / Math.max(1f - tLum, 0.001f);
            return new float[]{
                textR + s * (1f - textR),
                textG + s * (1f - textG),
                textB + s * (1f - textB)
            };
        }
    }
}
