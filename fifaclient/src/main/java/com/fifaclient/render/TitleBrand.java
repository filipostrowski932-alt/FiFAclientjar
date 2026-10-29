package com.fifaclient.render;

import net.minecraft.client.gui.DrawContext;

/** Animated FIFACLIENT wordmark drawn on the title screen. */
public final class TitleBrand {
    private TitleBrand() {
    }

    public static void render(DrawContext c) {
        Draw.alphaMul = 1f;
        String name = "FIFACLIENT";
        int scale = 3;
        int x = 14, y = 12;
        int tw = Draw.width(name) * scale;

        Draw.glow(c, x - 4, y - 4, tw + 8, 8 * scale + 8, 6, Theme.accent(0.1f), 10, 0.10f);
        Draw.gradientText(c, name, x, y, scale, 0f, true);
        Draw.hBar(c, x, y + 8 * scale + 3, tw, 2, 0f);
        Draw.textScaled(c, "Visual client  |  Fabric 1.21.11  |  Right Shift = menu", x + 1, y + 8 * scale + 9, 0.8f, Theme.MUTED, true);
    }
}
