package com.fifaclient.modules.hud;

import com.fifaclient.core.ClientModule;
import com.fifaclient.core.ModuleManager;
import com.fifaclient.render.Draw;
import com.fifaclient.render.Theme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ArrayListHud extends HudModule {
    public ArrayListHud() {
        super("ModuleList", "Gradient list of everything that is enabled.", true, 1, 0, 6, 6);
        this.listed = false;
    }

    @Override
    public void renderHud(DrawContext c, MinecraftClient mc) {
        List<ClientModule> list = new ArrayList<>();
        for (ClientModule m : ModuleManager.all()) {
            if (m.isEnabled() && m.listed) list.add(m);
        }
        if (list.isEmpty()) {
            w = 1;
            h = 1;
            return;
        }
        list.sort(Comparator.comparingInt((ClientModule m) -> -Draw.width(m.name)));

        int maxW = 0;
        for (ClientModule m : list) maxW = Math.max(maxW, Draw.width(m.name) + 10);

        int cy = 0, i = 0;
        for (ClientModule m : list) {
            int rw = Draw.width(m.name) + 10;
            int rx = maxW - rw;
            int col = Theme.accent(i * 0.08f);
            Draw.roundRect(c, rx, cy, rw, 12, 3, 0xB00E1116);
            Draw.roundRect(c, maxW - 2, cy + 2, 2, 8, 1, col);
            Draw.text(c, m.name, rx + 4, cy + 2, col, true);
            cy += 13;
            i++;
        }
        w = maxW;
        h = cy;
    }
}
