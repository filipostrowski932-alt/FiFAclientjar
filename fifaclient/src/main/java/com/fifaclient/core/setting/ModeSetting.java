package com.fifaclient.core.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class ModeSetting extends Setting {
    public final String[] modes;
    private int index;

    public ModeSetting(String name, String def, String... modes) {
        super(name);
        this.modes = modes;
        for (int i = 0; i < modes.length; i++) {
            if (modes[i].equalsIgnoreCase(def)) index = i;
        }
    }

    public String get() {
        return modes[index];
    }

    public int index() {
        return index;
    }

    public boolean is(String mode) {
        return get().equalsIgnoreCase(mode);
    }

    public void next() {
        index = (index + 1) % modes.length;
    }

    public void prev() {
        index = (index - 1 + modes.length) % modes.length;
    }

    @Override
    public JsonElement save() {
        return new JsonPrimitive(get());
    }

    @Override
    public void load(JsonElement e) {
        try {
            if (e == null || !e.isJsonPrimitive()) return;
            String s = e.getAsString();
            for (int i = 0; i < modes.length; i++) {
                if (modes[i].equalsIgnoreCase(s)) index = i;
            }
        } catch (Exception ignored) {
        }
    }
}
