package com.fifaclient.modules.movement;

import com.fifaclient.core.Category;
import com.fifaclient.core.ClientModule;
import net.minecraft.client.MinecraftClient;

/** Sprints automatically while you hold forward - the same thing you'd do by holding the sprint key. */
public class ToggleSprintModule extends ClientModule {
    public ToggleSprintModule() {
        super("ToggleSprint", "Automatically sprint while walking forward.", Category.MOVEMENT, true);
    }

    @Override
    public void onTick(MinecraftClient mc) {
        var p = mc.player;
        if (p == null || !isEnabled()) return;
        boolean canSprint = p.getHungerManager().getFoodLevel() > 6 || p.getAbilities().allowFlying;
        if (mc.options.forwardKey.isPressed() && canSprint
                && !p.isSneaking() && !p.isUsingItem() && !p.horizontalCollision) {
            p.setSprinting(true);
        }
    }
}
