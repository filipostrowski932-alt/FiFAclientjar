package com.fifaclient.modules.hud;

import com.fifaclient.core.Category;
import com.fifaclient.core.ClientModule;
import com.fifaclient.core.setting.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/**
 * A draggable HUD element. renderHud() draws at local origin (0,0) and must set w/h.
 * Position/scale are handled here, so subclasses stay simple.
 */
public abstract class HudModule extends ClientModule {
    public float x = Float.NaN, y = Float.NaN;
    public int w = 20, h = 12;

    public final NumberSetting scale = add(new NumberSetting("Scale", 1.0, 0.6, 2.0, 0.1));

    private final int anchorX, anchorY, offX, offY;

    /**
     * @param anchorX 0 = left, 1 = right, 2 = center
     * @param anchorY 0 = top, 1 = bottom
     */
    protected HudModule(String name, String description, boolean def, int anchorX, int anchorY, int offX, int offY) {
        super(name, description, Category.HUD, def);
        this.anchorX = anchorX;
        this.anchorY = anchorY;
        this.offX = offX;
        this.offY = offY;
    }

    public abstract void renderHud(DrawContext c, MinecraftClient mc);

    public final void resetPosition() {
        x = Float.NaN;
        y = Float.NaN;
    }

    public final void draw(DrawContext c, MinecraftClient mc) {
        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();
        float s = scale.getF();
        var m = c.getMatrices();

        if (Float.isNaN(x) || Float.isNaN(y)) {
            // measuring pass off-screen so we know w/h before placing the default position
            m.pushMatrix();
            m.translate(-4000f, -4000f);
            m.scale(s, s);
            renderHud(c, mc);
            m.popMatrix();
            float bw = w * s, bh = h * s;
            x = anchorX == 0 ? offX : anchorX == 1 ? sw - bw - offX : (sw - bw) / 2f;
            y = anchorY == 0 ? offY : sh - bh - offY;
            return;
        }

        x = Math.max(0f, Math.min(x, Math.max(0f, sw - w * s)));
        y = Math.max(0f, Math.min(y, Math.max(0f, sh - h * s)));

        m.pushMatrix();
        m.translate(x, y);
        m.scale(s, s);
        renderHud(c, mc);
        m.popMatrix();
    }
}
