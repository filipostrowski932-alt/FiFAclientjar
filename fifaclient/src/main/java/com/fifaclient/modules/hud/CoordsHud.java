package com.fifaclient.modules.hud;

import com.fifaclient.render.Draw;
import com.fifaclient.render.Theme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.Direction;

import java.util.Locale;

public class CoordsHud extends HudModule {
    public CoordsHud() {
        super("Coordinates", "XYZ and the direction you face.", true, 0, 1, 6, 6);
    }

    @Override
    public void renderHud(DrawContext c, MinecraftClient mc) {
        if (mc.player == null) return;
        Direction d = mc.player.getHorizontalFacing();
        String axis = switch (d) {
            case NORTH -> "-Z";
            case SOUTH -> "+Z";
            case EAST -> "+X";
            case WEST -> "-X";
            default -> "";
        };
        String name = d.asString();
        String dir = Character.toUpperCase(name.charAt(0)) + name.substring(1) + " (" + axis + ")";

        String[] labels = {"X", "Y", "Z", "DIR"};
        String[] values = {
                String.format(Locale.ROOT, "%.1f", mc.player.getX()),
                String.format(Locale.ROOT, "%.1f", mc.player.getY()),
                String.format(Locale.ROOT, "%.1f", mc.player.getZ()),
                dir
        };

        int maxV = 0;
        for (String v : values) maxV = Math.max(maxV, Draw.width(v));
        w = 8 + 26 + maxV + 8;
        h = 8 + labels.length * 11;

        Draw.roundRect(c, 0, 0, w, h, 5, Theme.PANEL);
        Draw.roundRect(c, 0, 4, 2, h - 8, 1, Theme.accent(0.3f));
        for (int i = 0; i < labels.length; i++) {
            int ty = 5 + i * 11;
            Draw.text(c, labels[i], 8, ty, Theme.accent(i * 0.12f), false);
            Draw.text(c, values[i], 8 + 26, ty, Theme.TEXT, false);
        }
    }
}
