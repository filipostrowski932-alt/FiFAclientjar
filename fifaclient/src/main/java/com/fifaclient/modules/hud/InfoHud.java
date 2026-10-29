package com.fifaclient.modules.hud;

import com.fifaclient.render.Draw;
import com.fifaclient.render.Theme;
import com.fifaclient.util.Cps;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;
import java.util.function.ToIntFunction;

/** Small "LABEL value" pill. One class powers FPS, Ping, CPS and Clock. */
public class InfoHud extends HudModule {
    private final String label;
    private final Function<MinecraftClient, String> value;
    private final ToIntFunction<MinecraftClient> color;

    public InfoHud(String name, String description, String label, boolean def, int offY,
                   Function<MinecraftClient, String> value, ToIntFunction<MinecraftClient> color) {
        super(name, description, def, 0, 0, 6, offY);
        this.label = label;
        this.value = value;
        this.color = color;
    }

    @Override
    public void renderHud(DrawContext c, MinecraftClient mc) {
        String v = value.apply(mc);
        int lw = Draw.width(label);
        int vw = Draw.width(v);
        w = 8 + lw + 6 + vw + 6;
        h = 16;

        Draw.roundRect(c, 0, 0, w, h, 4, Theme.PANEL);
        Draw.roundRect(c, 0, 3, 2, h - 6, 1, Theme.accent(0.1f));
        Draw.text(c, label, 8, 4, Theme.MUTED, false);
        int col = color == null ? Theme.TEXT : color.applyAsInt(mc);
        Draw.text(c, v, 8 + lw + 6, 4, col, false);
    }

    // ---------- presets ----------

    public static InfoHud fps() {
        return new InfoHud("FPS", "Frames per second.", "FPS", true, 32,
                mc -> String.valueOf(mc.getCurrentFps()),
                mc -> mc.getCurrentFps() >= 60 ? Theme.GOOD : mc.getCurrentFps() >= 30 ? 0xFFFFC447 : Theme.BAD);
    }

    public static InfoHud ping() {
        return new InfoHud("Ping", "Latency to the server.", "PING", true, 52,
                mc -> latency(mc) + " ms",
                mc -> latency(mc) <= 80 ? Theme.GOOD : latency(mc) <= 160 ? 0xFFFFC447 : Theme.BAD);
    }

    public static InfoHud cps() {
        return new InfoHud("CPS", "Clicks per second (left | right).", "CPS", true, 72,
                mc -> Cps.left() + " | " + Cps.right(), null);
    }

    public static InfoHud clock() {
        DateTimeFormatter f = DateTimeFormatter.ofPattern("HH:mm");
        return new InfoHud("Clock", "Your real-world time.", "TIME", false, 92,
                mc -> LocalTime.now().format(f), null);
    }

    private static int latency(MinecraftClient mc) {
        if (mc.player == null || mc.getNetworkHandler() == null) return 0;
        PlayerListEntry e = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
        return e == null ? 0 : e.getLatency();
    }
}
