package com.fifaclient.modules.hud;

import com.fifaclient.core.setting.BoolSetting;
import com.fifaclient.render.Draw;
import com.fifaclient.render.Theme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class WatermarkHud extends HudModule {
    public final BoolSetting info = add(new BoolSetting("Player + FPS", true));

    public WatermarkHud() {
        super("Watermark", "Animated FIFACLIENT logo in the corner.", true, 0, 0, 6, 6);
    }

    @Override
    public void renderHud(DrawContext c, MinecraftClient mc) {
        String name = "FIFACLIENT";
        String extra = "";
        if (info.get() && mc.player != null) {
            extra = "  |  " + mc.player.getName().getString() + "  |  " + mc.getCurrentFps() + " FPS";
        }
        int nameW = Draw.width(name);
        w = 16 + nameW + Draw.width(extra);
        h = 20;

        Draw.glow(c, 0, 0, w, h, 5, Theme.accent(0.2f), 8, 0.14f);
        Draw.roundRect(c, 0, 0, w, h, 5, Theme.PANEL);
        Draw.gradientText(c, name, 8, 6, 1f, 0f, true);
        if (!extra.isEmpty()) Draw.text(c, extra, 8 + nameW, 6, Theme.MUTED, true);
        Draw.hBar(c, 5, h - 2, w - 10, 1, 0f);
    }
}
