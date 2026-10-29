package com.fifaclient.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/** Small immediate-mode drawing toolkit: rounded rects, glow, gradient text. */
public final class Draw {
    /** Global alpha multiplier (used for fade-in animations). */
    public static float alphaMul = 1f;

    private Draw() {
    }

    // ---------- colour helpers ----------

    public static int mul(int c) {
        if (alphaMul >= 0.999f) return c;
        int a = (int) (((c >>> 24) & 0xFF) * Math.max(0f, alphaMul));
        return (a << 24) | (c & 0xFFFFFF);
    }

    public static int alpha(int c, float a) {
        int al = (int) (((c >>> 24) & 0xFF) * Math.max(0f, Math.min(1f, a)));
        return (al << 24) | (c & 0xFFFFFF);
    }

    private static int ch(int c, int shift) {
        return (c >>> shift) & 0xFF;
    }

    public static int lerp(int a, int b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int out = 0;
        for (int shift = 0; shift <= 24; shift += 8) {
            int ca = ch(a, shift), cb = ch(b, shift);
            out |= ((int) (ca + (cb - ca) * t) & 0xFF) << shift;
        }
        return out;
    }

    public static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    // ---------- primitives ----------

    public static void fill(DrawContext c, int x1, int y1, int x2, int y2, int color) {
        if (x2 <= x1 || y2 <= y1) return;
        c.fill(x1, y1, x2, y2, mul(color));
    }

    public static void fillGradient(DrawContext c, int x1, int y1, int x2, int y2, int top, int bottom) {
        c.fillGradient(x1, y1, x2, y2, mul(top), mul(bottom));
    }

    public static void rect(DrawContext c, int x, int y, int w, int h, int color) {
        fill(c, x, y, x + w, y + h, color);
    }

    public static void outline(DrawContext c, int x, int y, int w, int h, int color) {
        fill(c, x, y, x + w, y + 1, color);
        fill(c, x, y + h - 1, x + w, y + h, color);
        fill(c, x, y, x + 1, y + h, color);
        fill(c, x + w - 1, y, x + w, y + h, color);
    }

    private static int cornerInset(int r, int i) {
        double dy = r - i - 0.5;
        return r - (int) Math.round(Math.sqrt(Math.max(0, r * r - dy * dy)));
    }

    public static void roundRect(DrawContext c, int x, int y, int w, int h, int r, int color) {
        if (w <= 0 || h <= 0) return;
        r = Math.max(0, Math.min(r, Math.min(w, h) / 2));
        if (r == 0) {
            rect(c, x, y, w, h, color);
            return;
        }
        for (int i = 0; i < r; i++) {
            int inset = cornerInset(r, i);
            fill(c, x + inset, y + i, x + w - inset, y + i + 1, color);
            fill(c, x + inset, y + h - 1 - i, x + w - inset, y + h - i, color);
        }
        fill(c, x, y + r, x + w, y + h - r, color);
    }

    /** Rounded rect with a vertical colour gradient (use for small shapes only). */
    public static void roundRectV(DrawContext c, int x, int y, int w, int h, int r, int top, int bottom) {
        if (w <= 0 || h <= 0) return;
        r = Math.max(0, Math.min(r, Math.min(w, h) / 2));
        for (int row = 0; row < h; row++) {
            int inset = 0;
            if (row < r) inset = cornerInset(r, row);
            else if (row >= h - r) inset = cornerInset(r, h - 1 - row);
            float t = h <= 1 ? 0f : row / (float) (h - 1);
            fill(c, x + inset, y + row, x + w - inset, y + row + 1, lerp(top, bottom, t));
        }
    }

    /** Soft glow around a rounded rect. */
    public static void glow(DrawContext c, int x, int y, int w, int h, int r, int color, int size, float strength) {
        float perLayer = strength / size * 2f;
        for (int i = size; i >= 2; i -= 2) {
            roundRect(c, x - i, y - i, w + i * 2, h + i * 2, r + i, alpha(color, perLayer));
        }
    }

    /** Horizontal bar filled with the animated accent gradient. */
    public static void hBar(DrawContext c, int x, int y, int w, int h, float offset) {
        for (int i = 0; i < w; i += 2) {
            int seg = Math.min(2, w - i);
            fill(c, x + i, y, x + i + seg, y + h, Theme.accent(offset + (i / (float) Math.max(1, w)) * 0.6f));
        }
    }

    // ---------- text ----------

    private static MinecraftClient mc() {
        return MinecraftClient.getInstance();
    }

    public static int width(String s) {
        return mc().textRenderer.getWidth(s);
    }

    public static void text(DrawContext c, String s, int x, int y, int color, boolean shadow) {
        int col = mul(color);
        // vanilla treats near-zero alpha as opaque, so skip instead of flashing
        if (((col >>> 24) & 0xFF) < 8) return;
        c.drawText(mc().textRenderer, s, x, y, col, shadow);
    }

    public static void textScaled(DrawContext c, String s, float x, float y, float scale, int color, boolean shadow) {
        var m = c.getMatrices();
        m.pushMatrix();
        m.translate(x, y);
        m.scale(scale, scale);
        text(c, s, 0, 0, color, shadow);
        m.popMatrix();
    }

    /** Text where every letter walks through the accent gradient. */
    public static void gradientText(DrawContext c, String s, float x, float y, float scale, float offset, boolean shadow) {
        var m = c.getMatrices();
        m.pushMatrix();
        m.translate(x, y);
        m.scale(scale, scale);
        int cx = 0;
        for (int i = 0; i < s.length(); i++) {
            String ch = String.valueOf(s.charAt(i));
            text(c, ch, cx, 0, Theme.accent(offset + i * 0.07f), shadow);
            cx += width(ch);
        }
        m.popMatrix();
    }
}
