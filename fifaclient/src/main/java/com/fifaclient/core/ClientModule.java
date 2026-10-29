package com.fifaclient.core;

import com.fifaclient.core.setting.Setting;
import com.fifaclient.render.Notifications;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;

/** Base class for every FIFACLIENT feature. */
public abstract class ClientModule {
    public final String name;
    public final String description;
    public final Category category;
    public final List<Setting> settings = new ArrayList<>();

    /** false = always on and hidden toggle (e.g. Appearance). */
    public boolean toggleable = true;
    /** shown in the ModuleList HUD. */
    public boolean listed = true;

    private boolean enabled;

    protected ClientModule(String name, String description, Category category, boolean defaultEnabled) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.enabled = defaultEnabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean value) {
        if (!toggleable || enabled == value) return;
        enabled = value;
        if (value) onEnable();
        else onDisable();
        Notifications.moduleToggled(this);
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    /** Used by the config loader: no hooks, no notification. */
    public void loadEnabled(boolean value) {
        if (toggleable) enabled = value;
    }

    protected <T extends Setting> T add(T setting) {
        settings.add(setting);
        return setting;
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    /** Called every client tick, even when disabled (modules check themselves). */
    public void onTick(MinecraftClient mc) {
    }

    /** Called every rendered frame while in a world. */
    public void onFrame(MinecraftClient mc) {
    }

    public void onShutdown(MinecraftClient mc) {
    }
}
