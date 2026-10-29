package com.fifaclient.gui;

import com.fifaclient.core.Config;
import com.fifaclient.core.ModuleManager;
import com.fifaclient.modules.hud.HudModule;
import com.fifaclient.render.Draw;
import com.fifaclient.render.Theme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;

import java.util.List;

/** Drag HUD elements around. Scroll = resize, right-click = reset position. */
public class HudEditorScreen extends Screen {
    private final Screen parent;
    private HudModule active;
    private float grabX, grabY;

    public HudEditorScreen(Screen parent) {
        super(Text.literal("HUD Editor"));
        this.parent = parent;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
    }

    @Override
    public void close() {
        Config.save();
        MinecraftClient.getInstance().setScreen(parent);
    }

    @Override
    public void removed() {
        Draw.alphaMul = 1f;
        Config.save();
    }

    private HudModule hovered(double mx, double my) {
        List<HudModule> list = ModuleManager.huds();
        for (int i = list.size() - 1; i >= 0; i--) {
            HudModule m = list.get(i);
            if (!m.isEnabled() || Float.isNaN(m.x) || Float.isNaN(m.y)) continue;
            float s = m.scale.getF();
            if (Draw.inside(mx, my, (int) m.x, (int) m.y, (int) (m.w * s), (int) (m.h * s))) return m;
        }
        return null;
    }

    @Override
    public void render(DrawContext c, int mx, int my, float delta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        Draw.alphaMul = 1f;
        Draw.fill(c, 0, 0, width, height, 0x66000000);

        int guide = Draw.alpha(Theme.accent(), 0.20f);
        Draw.fill(c, width / 2, 0, width / 2 + 1, height, guide);
        Draw.fill(c, 0, height / 2, width, height / 2 + 1, guide);

        String title = "HUD EDITOR";
        Draw.gradientText(c, title, (width - Draw.width(title) * 2) / 2f, 14, 2f, 0f, true);
        String hint = "Drag to move   |   Scroll to resize   |   Right-click to reset   |   ESC to go back";
        Draw.text(c, hint, (width - Draw.width(hint)) / 2, 36, Theme.MUTED, true);

        if (mc.player == null) return;
        HudModule hover = active != null ? active : hovered(mx, my);
        for (HudModule m : ModuleManager.huds()) {
            if (!m.isEnabled()) continue;
            m.draw(c, mc);
            if (Float.isNaN(m.x) || Float.isNaN(m.y)) continue;
            float s = m.scale.getF();
            int bx = (int) m.x, by = (int) m.y, bw = (int) (m.w * s), bh = (int) (m.h * s);
            boolean hot = m == hover;
            Draw.outline(c, bx - 2, by - 2, bw + 4, bh + 4, hot ? Theme.accent() : Draw.alpha(Theme.TEXT, 0.25f));
            if (hot) Draw.text(c, m.name + "  x" + m.scale.display(), bx, by - 13, Theme.TEXT, true);
        }
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        HudModule m = hovered(click.x(), click.y());
        if (m != null) {
            if (click.button() == 1) {
                m.resetPosition();
            } else if (click.button() == 0) {
                active = m;
                grabX = (float) click.x() - m.x;
                grabY = (float) click.y() - m.y;
            }
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        if (active == null) return super.mouseDragged(click, offsetX, offsetY);
        float s = active.scale.getF();
        float bw = active.w * s, bh = active.h * s;
        float nx = (float) click.x() - grabX;
        float ny = (float) click.y() - grabY;

        // snap to screen centre lines
        if (Math.abs(nx + bw / 2f - width / 2f) < 5f) nx = width / 2f - bw / 2f;
        if (Math.abs(ny + bh / 2f - height / 2f) < 5f) ny = height / 2f - bh / 2f;

        active.x = Math.max(0f, Math.min(nx, width - bw));
        active.y = Math.max(0f, Math.min(ny, height - bh));
        return true;
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (active != null) {
            active = null;
            Config.save();
            return true;
        }
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        HudModule m = hovered(mouseX, mouseY);
        if (m != null) {
            m.scale.set(m.scale.get() + Math.signum(verticalAmount) * 0.1);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (input.isEscape()) {
            close();
            return true;
        }
        return super.keyPressed(input);
    }
}
