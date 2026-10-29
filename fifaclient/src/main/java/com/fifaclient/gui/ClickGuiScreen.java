package com.fifaclient.gui;

import com.fifaclient.FifaClient;
import com.fifaclient.core.Category;
import com.fifaclient.core.ClientModule;
import com.fifaclient.core.Config;
import com.fifaclient.core.ModuleManager;
import com.fifaclient.core.setting.BoolSetting;
import com.fifaclient.core.setting.ModeSetting;
import com.fifaclient.core.setting.NumberSetting;
import com.fifaclient.core.setting.Setting;
import com.fifaclient.modules.client.AppearanceModule;
import com.fifaclient.render.Anim;
import com.fifaclient.render.Draw;
import com.fifaclient.render.Theme;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.DoubleConsumer;

/**
 * The big animated ClickGUI. Fully immediate-mode: every frame it draws and records clickable
 * regions ("hits"), so layout and input can never get out of sync.
 */
public class ClickGuiScreen extends Screen {
    private interface ClickAction {
        void run(int button, double mouseX);
    }

    private static final class Hit {
        final int x, y, w, h;
        final boolean scrolls;
        final ClickAction click;
        final DoubleConsumer drag;

        Hit(int x, int y, int w, int h, boolean scrolls, ClickAction click, DoubleConsumer drag) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.scrolls = scrolls;
            this.click = click;
            this.drag = drag;
        }

        boolean contains(double mx, double my) {
            return Draw.inside(mx, my, x, y, w, h);
        }
    }

    private final Anim open = new Anim(0);
    private final Anim pill = new Anim(0);
    private final Anim scrollAnim = new Anim(0);
    private final Map<ClientModule, Anim> expandAnims = new HashMap<>();
    private final Map<ClientModule, Anim> toggleAnims = new HashMap<>();
    private final Map<Setting, Anim> settingAnims = new HashMap<>();
    private final Set<ClientModule> expanded = new HashSet<>();
    private final List<Hit> hits = new ArrayList<>();

    private Hit dragging;
    private Category tab = Category.HUD;
    private float scrollTarget;
    private int cx0, cy0, cx1, cy1;

    public ClickGuiScreen() {
        super(Text.literal("FIFACLIENT"));
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    /** No vanilla blur/background - we paint our own. */
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
    }

    @Override
    public void removed() {
        Draw.alphaMul = 1f;
        Config.save();
    }

    // ------------------------------------------------------------------ layout helpers

    private static int rowHeight(Setting s) {
        return s instanceof NumberSetting ? 32 : 22;
    }

    private static int settingsHeight(ClientModule m) {
        if (m.settings.isEmpty()) return 0;
        int h = 8;
        for (Setting s : m.settings) h += rowHeight(s);
        return h;
    }

    // ------------------------------------------------------------------ rendering

    @Override
    public void render(DrawContext c, int mx, int my, float delta) {
        hits.clear();
        float o = open.to(1f, 14f);
        Draw.alphaMul = o;

        // backdrop
        int dim = (int) (AppearanceModule.dim() * 255f);
        Draw.fill(c, 0, 0, width, height, (dim << 24) | 0x04060A);
        Draw.fillGradient(c, 0, height / 2, width, height, 0x00000000, Draw.alpha(Theme.accent(), 0.20f));

        int ww = Math.min(640, width - 24), wh = Math.min(380, height - 24);
        int wx = (width - ww) / 2, wy = (height - wh) / 2;
        int rad = AppearanceModule.round() + 3;

        var m = c.getMatrices();
        float sc = 0.94f + 0.06f * o;
        m.pushMatrix();
        m.translate(width / 2f, height / 2f);
        m.scale(sc, sc);
        m.translate(-width / 2f, -height / 2f);

        // window
        Draw.glow(c, wx, wy, ww, wh, rad, Theme.accent(0.25f), 10, 0.16f);
        Draw.roundRect(c, wx - 1, wy - 1, ww + 2, wh + 2, rad + 1, Draw.alpha(Theme.accent(0f), 0.55f));
        Draw.roundRect(c, wx, wy, ww, wh, rad, Theme.BG);

        drawHeader(c, wx, wy, ww, mx, my);
        drawSidebar(c, wx, wy, wh, mx, my);

        cx0 = wx + 120;
        cy0 = wy + 56;
        cx1 = wx + ww - 12;
        cy1 = wy + wh - 12;
        drawContent(c, mx, my);

        m.popMatrix();
        Draw.alphaMul = 1f;
    }

    private void drawHeader(DrawContext c, int wx, int wy, int ww, int mx, int my) {
        Draw.gradientText(c, "FIFACLIENT", wx + 18, wy + 11, 2.2f, 0f, true);
        Draw.textScaled(c, "v" + FifaClient.VERSION + "  |  Fabric 1.21.11", wx + 20, wy + 33, 0.75f, Theme.MUTED, false);
        Draw.hBar(c, wx + 12, wy + 46, ww - 24, 1, 0f);

        int r = AppearanceModule.round();
        // close button
        int cbx = wx + ww - 12 - 18, cby = wy + 14;
        boolean hc = Draw.inside(mx, my, cbx, cby, 18, 18);
        Draw.roundRect(c, cbx, cby, 18, 18, r, hc ? Theme.BAD : Theme.CARD);
        Draw.text(c, "x", cbx + 6, cby + 5, Theme.TEXT, false);
        hits.add(new Hit(cbx, cby, 18, 18, false, (b, x) -> close(), null));

        // HUD editor button
        int bw = 84, bx = cbx - 8 - bw, by = cby;
        boolean hh = Draw.inside(mx, my, bx, by, bw, 18);
        Draw.roundRect(c, bx, by, bw, 18, r, hh ? Theme.CARD_HOVER : Theme.CARD);
        Draw.outline(c, bx, by, bw, 18, Draw.alpha(Theme.accent(0.3f), hh ? 0.9f : 0.35f));
        String label = "HUD Editor";
        Draw.text(c, label, bx + (bw - Draw.width(label)) / 2, by + 5, Theme.TEXT, false);
        hits.add(new Hit(bx, by, bw, 18, false,
                (b, x) -> MinecraftClient.getInstance().setScreen(new HudEditorScreen(this)), null));
    }

    private void drawSidebar(DrawContext c, int wx, int wy, int wh, int mx, int my) {
        Category[] cats = Category.values();
        int tabsY = wy + 58;
        float pillY = pill.to(tab.ordinal() * 30f, 16f);
        int py = tabsY + (int) pillY;
        Draw.roundRectV(c, wx + 10, py, 98, 26, 5, Draw.alpha(Theme.accent(0f), 0.30f), Draw.alpha(Theme.accent(0.4f), 0.10f));
        Draw.roundRect(c, wx + 10, py + 5, 2, 16, 1, Theme.accent(0f));

        for (int i = 0; i < cats.length; i++) {
            final Category cat = cats[i];
            int ty = tabsY + i * 30;
            boolean sel = cat == tab;
            boolean hov = Draw.inside(mx, my, wx + 10, ty, 98, 26);
            Draw.text(c, cat.label, wx + 22, ty + 9, sel ? Theme.TEXT : hov ? 0xFFC5CAD3 : Theme.MUTED, false);
            hits.add(new Hit(wx + 10, ty, 98, 26, false, (b, x) -> {
                tab = cat;
                scrollTarget = 0f;
                scrollAnim.snap(0f);
            }, null));
        }

        Draw.textScaled(c, "Right Shift to close", wx + 14, wy + wh - 22, 0.7f, Theme.MUTED, false);
        Draw.textScaled(c, "Right-click = settings", wx + 14, wy + wh - 13, 0.7f, Theme.MUTED, false);
    }

    private void drawContent(DrawContext c, int mx, int my) {
        List<ClientModule> mods = ModuleManager.of(tab);
        int viewH = cy1 - cy0;

        int total = 0;
        for (ClientModule mod : mods) {
            Anim ex = expandAnims.computeIfAbsent(mod, k -> new Anim(0));
            total += 32 + (int) (settingsHeight(mod) * ex.value) + 6;
        }
        float maxScroll = Math.max(0, total - viewH);
        scrollTarget = Math.max(0f, Math.min(scrollTarget, maxScroll));
        float scroll = scrollAnim.to(scrollTarget, 18f);

        c.enableScissor(cx0, cy0, cx1, cy1);
        int y = cy0 - (int) scroll;
        for (ClientModule mod : mods) {
            y = drawModule(c, mod, y, mx, my);
        }
        c.disableScissor();

        if (maxScroll > 0) {
            int trackH = viewH;
            int barH = Math.max(16, (int) (trackH * (viewH / (float) total)));
            int barY = cy0 + (int) ((trackH - barH) * (scroll / maxScroll));
            Draw.roundRect(c, cx1 - 3, barY, 3, barH, 1, Draw.alpha(Theme.accent(0.2f), 0.7f));
        }
    }

    private int drawModule(DrawContext c, ClientModule mod, int y, int mx, int my) {
        Anim ex = expandAnims.computeIfAbsent(mod, k -> new Anim(0));
        Anim tg = toggleAnims.computeIfAbsent(mod, k -> new Anim(mod.isEnabled() ? 1f : 0f));
        float e = ex.to(expanded.contains(mod) ? 1f : 0f, 16f);
        float t = tg.to(mod.isEnabled() ? 1f : 0f, 18f);

        boolean hasSettings = !mod.settings.isEmpty();
        int x = cx0, w = cx1 - cx0 - 6;
        int h = 32 + (int) (settingsHeight(mod) * e);
        int r = AppearanceModule.round();

        if (y + h < cy0 || y > cy1) return y + h + 6;

        boolean inClip = Draw.inside(mx, my, cx0, cy0, cx1 - cx0, cy1 - cy0);
        boolean hover = inClip && Draw.inside(mx, my, x, y, w, 32);
        Draw.roundRect(c, x, y, w, h, r, hover ? Theme.CARD_HOVER : Theme.CARD);
        if (t > 0.02f) Draw.roundRect(c, x, y + 7, 3, 18, 1, Draw.alpha(Theme.accent(y * 0.004f), t));

        Draw.text(c, mod.name, x + 12, y + 7, Theme.TEXT, false);
        Draw.textScaled(c, mod.description, x + 12, y + 19, 0.75f, Theme.MUTED, false);

        // header click: expand (or toggle if there is nothing to expand)
        hits.add(new Hit(x, y, w, 32, true, (b, px) -> {
            if (b == 1 || (b == 0 && hasSettings)) {
                if (hasSettings && !expanded.remove(mod)) expanded.add(mod);
            } else if (b == 0) {
                mod.toggle();
            }
        }, null));

        int rightEdge = x + w - 12;
        if (mod.toggleable) {
            int sx = rightEdge - 30;
            drawSwitch(c, sx, y + 9, 30, 14, t);
            hits.add(new Hit(sx - 4, y + 4, 38, 24, true, (b, px) -> {
                if (b == 0) mod.toggle();
            }, null));
            rightEdge = sx - 10;
        } else {
            String s = "ALWAYS ON";
            Draw.textScaled(c, s, rightEdge - Draw.width(s) * 0.7f, y + 12, 0.7f, Theme.MUTED, false);
            rightEdge -= (int) (Draw.width(s) * 0.7f) + 10;
        }
        if (hasSettings) {
            Draw.text(c, expanded.contains(mod) ? "v" : ">", rightEdge - 6, y + 12, Theme.MUTED, false);
        }

        if (e > 0.02f) {
            int sy = y + 36;
            for (Setting s : mod.settings) {
                int rh = rowHeight(s);
                if (sy + rh - 4 <= y + h) drawSetting(c, s, x + 12, sy, w - 24, mx, my);
                sy += rh;
            }
        }
        return y + h + 6;
    }

    private void drawSwitch(DrawContext c, int x, int y, int w, int h, float t) {
        Draw.roundRect(c, x, y, w, h, h / 2, Draw.lerp(0xFF2A303A, Theme.accent(x * 0.003f), t));
        int kx = x + 2 + (int) ((w - h) * t);
        Draw.roundRect(c, kx, y + 2, h - 4, h - 4, (h - 4) / 2, 0xFFFFFFFF);
    }

    private void drawSetting(DrawContext c, Setting s, int x, int y, int w, int mx, int my) {
        if (s instanceof BoolSetting b) {
            Anim a = settingAnims.computeIfAbsent(b, k -> new Anim(b.get() ? 1f : 0f));
            float t = a.to(b.get() ? 1f : 0f, 18f);
            Draw.text(c, b.name, x, y + 5, Theme.TEXT, false);
            drawSwitch(c, x + w - 26, y + 3, 26, 12, t);
            hits.add(new Hit(x, y, w, 18, true, (btn, px) -> b.toggle(), null));

        } else if (s instanceof ModeSetting md) {
            Draw.text(c, md.name, x, y + 5, Theme.TEXT, false);
            String cur = md.get();
            int pw = Draw.width(cur) + 16;
            int px = x + w - pw;
            Draw.roundRect(c, px, y + 1, pw, 16, 4, 0xFF222833);
            Draw.text(c, cur, px + 8, y + 5, Theme.accent(0.1f), false);
            hits.add(new Hit(px, y + 1, pw, 16, true, (btn, mxx) -> {
                if (btn == 1) md.prev();
                else md.next();
            }, null));

        } else if (s instanceof NumberSetting n) {
            Draw.text(c, n.name, x, y + 2, Theme.TEXT, false);
            String v = n.display();
            Draw.text(c, v, x + w - Draw.width(v), y + 2, Theme.accent(0f), false);
            double p = (n.get() - n.min) / (n.max - n.min);
            int ty = y + 18;
            Draw.roundRect(c, x, ty, w, 4, 2, 0xFF262C36);
            int fw = (int) (w * p);
            if (fw > 0) Draw.roundRect(c, x, ty, Math.max(4, fw), 4, 2, Theme.accent(0.2f));
            Draw.roundRect(c, x + fw - 4, ty - 3, 8, 10, 3, 0xFFFFFFFF);
            final int fx = x, fwid = w;
            hits.add(new Hit(x - 4, y + 12, w + 8, 18, true,
                    (btn, mxx) -> {
                        if (btn == 0) setNumber(n, mxx, fx, fwid);
                    },
                    mxx -> setNumber(n, mxx, fx, fwid)));
        }
    }

    private static void setNumber(NumberSetting n, double mouseX, int x, int w) {
        double p = Math.max(0.0, Math.min(1.0, (mouseX - x) / w));
        n.set(n.min + p * (n.max - n.min));
    }

    // ------------------------------------------------------------------ input (1.21.11 signatures)

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mx = click.x(), my = click.y();
        int button = click.button();
        for (int i = hits.size() - 1; i >= 0; i--) {
            Hit hit = hits.get(i);
            if (!hit.contains(mx, my)) continue;
            if (hit.scrolls && !Draw.inside(mx, my, cx0, cy0, cx1 - cx0, cy1 - cy0)) continue;
            hit.click.run(button, mx);
            if (hit.drag != null && button == 0) dragging = hit;
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        if (dragging != null && dragging.drag != null) {
            dragging.drag.accept(click.x());
            return true;
        }
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (dragging != null) {
            dragging = null;
            Config.save();
            return true;
        }
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scrollTarget -= (float) (verticalAmount * 30.0);
        return true;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        int bound = KeyBindingHelper.getBoundKeyOf(FifaClient.openGuiKey).getCode();
        if (input.isEscape() || input.getKeycode() == bound) {
            close();
            return true;
        }
        return super.keyPressed(input);
    }
}
