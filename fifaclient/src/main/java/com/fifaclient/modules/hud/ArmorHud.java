package com.fifaclient.modules.hud;

import com.fifaclient.core.setting.BoolSetting;
import com.fifaclient.render.Draw;
import com.fifaclient.render.Theme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

public class ArmorHud extends HudModule {
    private static final EquipmentSlot[] SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    public final BoolSetting vertical = add(new BoolSetting("Vertical", false));

    public ArmorHud() {
        super("Armor", "Your armor with durability bars.", true, 1, 1, 6, 6);
    }

    @Override
    public void renderHud(DrawContext c, MinecraftClient mc) {
        if (mc.player == null) return;
        boolean v = vertical.get();
        int size = 18, gap = 2;
        int span = 4 * size + 3 * gap;
        w = v ? size + 8 : span + 8;
        h = v ? span + 8 : size + 8;

        Draw.roundRect(c, 0, 0, w, h, 5, Theme.PANEL);
        for (int i = 0; i < SLOTS.length; i++) {
            int ix = 4 + (v ? 0 : i * (size + gap));
            int iy = 4 + (v ? i * (size + gap) : 0);
            ItemStack stack = mc.player.getEquippedStack(SLOTS[i]);
            if (stack.isEmpty()) {
                Draw.roundRect(c, ix + 2, iy + 2, size - 4, size - 4, 3, 0x30FFFFFF);
            } else {
                c.drawItem(stack, ix + 1, iy + 1);
                c.drawStackOverlay(mc.textRenderer, stack, ix + 1, iy + 1);
            }
        }
    }
}
