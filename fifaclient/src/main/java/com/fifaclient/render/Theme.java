package com.fifaclient.render;

import com.fifaclient.modules.client.AppearanceModule;

import java.awt.Color;

public final class Theme {
    public static final String[] NAMES = {"FIFA", "Neon", "Sunset", "Ocean", "Rainbow"};

    private static final int[][] PAIRS = {
            {0xFF1BE37A, 0xFF2D8CFF}, // FIFA: pitch green -> blue
            {0xFFFF2BD6, 0xFF2BE0FF}, // Neon
            {0xFFFF9A1F, 0xFFFF2E63}, // Sunset
            {0xFF00E5FF, 0xFF3D5AFE}  // Ocean
    };

    public static final int BG = 0xF0101318;
    public static final int CARD = 0xFF171B22;
    public static final int CARD_HOVER = 0xFF1D222B;
    public static final int PANEL = 0xB00E1116;
    public static final int TEXT = 0xFFEDEFF3;
    public static final int MUTED = 0xFF8B93A1;
    public static final int GOOD = 0xFF2BE37A;
    public static final int BAD = 0xFFFF4D5E;

    private Theme() {
    }

    public static double time() {
        return System.nanoTime() / 1_000_000_000.0;
    }

    /** Animated accent colour; offset shifts the phase so neighbours differ. */
    public static int accent(float offset) {
        int idx = AppearanceModule.themeIndex();
        if (idx >= PAIRS.length) {
            float h = (float) ((time() * 0.08 + offset) % 1.0);
            if (h < 0) h += 1f;
            return Color.HSBtoRGB(h, 0.65f, 1f) | 0xFF000000;
        }
        float t = (float) ((Math.sin(time() * 1.4 + offset * 6.2831853) + 1.0) / 2.0);
        return Draw.lerp(PAIRS[idx][0], PAIRS[idx][1], t);
    }

    public static int accent() {
        return accent(0f);
    }
}
