package com.fifaclient.modules.visual;

import com.fifaclient.core.Category;
import com.fifaclient.core.ClientModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

/**
 * Client-side only light boost (infinite, invisible night-vision effect).
 * Nothing is sent to the server. Check your server's rules before using it.
 */
public class FullbrightModule extends ClientModule {
    public FullbrightModule() {
        super("Fullbright", "Maximum brightness in caves and at night (client-side).", Category.VISUAL, false);
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (!isEnabled() || mc.player == null) return;
        if (mc.player.getStatusEffect(StatusEffects.NIGHT_VISION) == null) {
            mc.player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.NIGHT_VISION, StatusEffectInstance.INFINITE, 0, false, false, false));
        }
    }

    @Override
    public void onDisable() {
        remove(MinecraftClient.getInstance());
    }

    @Override
    public void onShutdown(MinecraftClient mc) {
        remove(mc);
    }

    private void remove(MinecraftClient mc) {
        if (mc == null || mc.player == null) return;
        StatusEffectInstance cur = mc.player.getStatusEffect(StatusEffects.NIGHT_VISION);
        if (cur != null && cur.isInfinite()) mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
    }
}
