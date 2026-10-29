package com.fifaclient.modules.hud;

import com.fifaclient.core.setting.BoolSetting;
import com.fifaclient.render.Anim;
import com.fifaclient.render.Draw;
import com.fifaclient.render.Theme;
import com.fifaclient.util.Cps;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class KeystrokesHud extends HudModule {
    private static final int K = 22, G = 2;

    public final BoolSetting mouse = add(new BoolSetting("Mouse Buttons", true));
    public final BoolSetting space = add(new BoolSetting("Spacebar", true));

    private final Anim[] anims = new Anim[7];

    public KeystrokesHud() {
        super("Keystrokes", "WASD, mouse and space with a smooth press animation.", true, 0, 0, 6, 112);
        for (int i = 0; i < anims.length; i++) anims[i] = new Anim(0);
    }

    @Override
    public void renderHud(DrawContext c, MinecraftClient mc) {
        var o = mc.options;
        int step = K + G;
        w = 3 * K + 2 * G;
        h = 2 * step + K;

        key(c, step, 0, K, K, "W", o.forwardKey.isPressed(), 0, null);
        key(c, 0, step, K, K, "A", o.leftKey.isPressed(), 1, null);
        key(c, step, step, K, K, "S", o.backKey.isPressed(), 2, null);
        key(c, 2 * step, step, K, K, "D", o.rightKey.isPressed(), 3, null);

        int ny = 2 * step;
        if (mouse.get()) {
            int bw = (w - G) / 2;
            key(c, 0, ny, bw, K, "LMB", Cps.leftDown, 4, Cps.left() + " CPS");
            key(c, bw + G, ny, w - bw - G, K, "RMB", Cps.rightDown, 5, Cps.right() + " CPS");
            ny += step;
            h = ny - G;
        }
        if (space.get()) {
            int sh = 12;
            key(c, 0, ny, w, sh, "", o.jumpKey.isPressed(), 6, null);
            float t = anims[6].value;
            Draw.rect(c, (w - 22) / 2, ny + sh / 2, 22, 1, Draw.lerp(Theme.TEXT, 0xFF0B0E12, t));
            h = ny + sh;
        }
    }

    private void key(DrawContext c, int x, int y, int kw, int kh, String label, boolean down, int idx, String sub) {
        float t = anims[idx].to(down ? 1f : 0f, 22f);
        int bg = Draw.lerp(Theme.PANEL, Draw.alpha(Theme.accent(idx * 0.15f), 0.92f), t);
        Draw.roundRect(c, x, y, kw, kh, 4, bg);
        int fg = Draw.lerp(Theme.TEXT, 0xFF0B0E12, t);
        if (sub == null) {
            if (!label.isEmpty()) Draw.text(c, label, x + (kw - Draw.width(label)) / 2, y + (kh - 8) / 2, fg, false);
        } else {
            Draw.text(c, label, x + (kw - Draw.width(label)) / 2, y + 3, fg, false);
            float sc = 0.65f;
            int sw = (int) (Draw.width(sub) * sc);
            Draw.textScaled(c, sub, x + (kw - sw) / 2f, y + kh - 9, sc, Draw.lerp(Theme.MUTED, 0xFF0B0E12, t), false);
        }
    }
}
