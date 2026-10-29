package com.fifaclient.render;

import com.fifaclient.core.ClientModule;
import com.fifaclient.modules.visual.NotificationsModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

/** Slide-in toasts shown when modules are toggled. */
public final class Notifications {
    private static final class Toast {
        final String title, message;
        final boolean good;
        final long born = System.currentTimeMillis();
        final Anim slide = new Anim(0);

        Toast(String title, String message, boolean good) {
            this.title = title;
            this.message = message;
            this.good = good;
        }
    }

    private static final List<Toast> TOASTS = new ArrayList<>();

    private Notifications() {
    }

    public static void push(String title, String message, boolean good) {
        TOASTS.add(new Toast(title, message, good));
        while (TOASTS.size() > 5) TOASTS.remove(0);
    }

    public static void moduleToggled(ClientModule m) {
        if (MinecraftClient.getInstance().player == null) return;
        if (!NotificationsModule.shouldShow()) return;
        push(m.name, m.isEnabled() ? "Enabled" : "Disabled", m.isEnabled());
    }

    public static void render(DrawContext c, MinecraftClient mc) {
        long now = System.currentTimeMillis();
        long life = NotificationsModule.durationMs();
        TOASTS.removeIf(t -> now - t.born > life + 600);

        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();
        int index = 0;
        for (int i = TOASTS.size() - 1; i >= 0; i--) {
            Toast t = TOASTS.get(i);
            long age = now - t.born;
            float s = t.slide.to(age > life ? 0f : 1f, 14f);
            int w = 140, h = 26;
            int x = (int) (sw - (w + 10) * s);
            int y = sh - 76 - index * (h + 6);
            int accent = t.good ? Theme.GOOD : Theme.BAD;

            Draw.glow(c, x, y, w, h, 5, accent, 6, 0.16f);
            Draw.roundRect(c, x, y, w, h, 5, 0xE00E1116);
            Draw.roundRect(c, x, y + 4, 3, h - 8, 1, accent);
            Draw.text(c, t.title, x + 10, y + 5, Theme.TEXT, false);
            Draw.text(c, t.message, x + 10, y + 15, accent, false);
            float left = Math.max(0f, 1f - age / (float) life);
            Draw.fill(c, x + 6, y + h - 2, x + 6 + (int) ((w - 12) * left), y + h - 1, Theme.accent());
            index++;
        }
    }
}
