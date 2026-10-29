package com.fifaclient.core;

import com.fifaclient.modules.client.AppearanceModule;
import com.fifaclient.modules.hud.*;
import com.fifaclient.modules.movement.ToggleSprintModule;
import com.fifaclient.modules.visual.FullbrightModule;
import com.fifaclient.modules.visual.NotificationsModule;
import com.fifaclient.modules.visual.ZoomModule;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;

public final class ModuleManager {
    private static final List<ClientModule> ALL = new ArrayList<>();
    private static final List<HudModule> HUDS = new ArrayList<>();
    private static boolean ready;

    private ModuleManager() {
    }

    public static void init() {
        if (ready) return;
        ready = true;

        // HUD
        register(new WatermarkHud(),
                InfoHud.fps(), InfoHud.ping(), InfoHud.cps(), InfoHud.clock(),
                new CoordsHud(), new KeystrokesHud(), new ArmorHud(), new ArrayListHud());
        // Visual
        register(new ZoomModule(), new FullbrightModule(), new NotificationsModule());
        // Movement
        register(new ToggleSprintModule());
        // Client
        register(new AppearanceModule());
    }

    private static void register(ClientModule... modules) {
        for (ClientModule m : modules) {
            ALL.add(m);
            if (m instanceof HudModule h) HUDS.add(h);
        }
    }

    public static List<ClientModule> all() {
        return ALL;
    }

    public static List<HudModule> huds() {
        return HUDS;
    }

    public static List<ClientModule> of(Category category) {
        List<ClientModule> out = new ArrayList<>();
        for (ClientModule m : ALL) {
            if (m.category == category) out.add(m);
        }
        return out;
    }

    public static void tick(MinecraftClient mc) {
        for (ClientModule m : ALL) m.onTick(mc);
    }

    public static void frame(MinecraftClient mc) {
        for (ClientModule m : ALL) m.onFrame(mc);
    }

    public static void shutdown(MinecraftClient mc) {
        for (ClientModule m : ALL) m.onShutdown(mc);
    }
}
