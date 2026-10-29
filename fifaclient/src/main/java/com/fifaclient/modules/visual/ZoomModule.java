package com.fifaclient.modules.visual;

import com.fifaclient.FifaClient;
import com.fifaclient.core.Category;
import com.fifaclient.core.ClientModule;
import com.fifaclient.core.setting.NumberSetting;
import com.fifaclient.mixin.SimpleOptionAccessor;
import com.fifaclient.render.Anim;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.SimpleOption;

/** Hold the zoom key (default C) for a smooth zoom. Restores your FOV afterwards. */
public class ZoomModule extends ClientModule {
    public final NumberSetting level = add(new NumberSetting("Zoom Level", 4.0, 1.5, 10.0, 0.5));
    public final NumberSetting speed = add(new NumberSetting("Smoothness", 12.0, 4.0, 24.0, 1.0));

    private final Anim anim = new Anim(0);
    private int baseFov = 70;
    private boolean modified;

    public ZoomModule() {
        super("Zoom", "Hold C to zoom in smoothly.", Category.VISUAL, true);
    }

    @Override
    public void onTick(MinecraftClient mc) {
        update(mc);
    }

    @Override
    public void onFrame(MinecraftClient mc) {
        update(mc);
    }

    @Override
    public void onDisable() {
        restore(MinecraftClient.getInstance());
    }

    @Override
    public void onShutdown(MinecraftClient mc) {
        restore(mc);
    }

    private void update(MinecraftClient mc) {
        if (mc.player == null || mc.options == null) {
            restore(mc);
            return;
        }
        boolean zooming = isEnabled() && FifaClient.zoomKey != null
                && FifaClient.zoomKey.isPressed() && mc.currentScreen == null;
        if (!modified && !zooming) return;

        SimpleOption<Integer> fov = mc.options.getFov();
        if (!modified) {
            baseFov = fov.getValue();
            modified = true;
            anim.snap(0f);
        }

        float t = anim.to(zooming ? 1f : 0f, speed.getF());
        if (!zooming && t <= 0.001f) {
            restore(mc);
            return;
        }
        double target = Math.max(4.0, baseFov / level.get());
        setRaw(fov, (int) Math.round(baseFov + (target - baseFov) * t));
    }

    private void restore(MinecraftClient mc) {
        if (!modified || mc == null || mc.options == null) return;
        setRaw(mc.options.getFov(), baseFov);
        modified = false;
        anim.snap(0f);
    }

    @SuppressWarnings("unchecked")
    private static void setRaw(SimpleOption<Integer> option, int value) {
        ((SimpleOptionAccessor<Integer>) (Object) option).fifaclient$setRaw(value);
    }
}
