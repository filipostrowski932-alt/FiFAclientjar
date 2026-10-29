package com.fifaclient.core.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.Locale;

public class NumberSetting extends Setting {
    public final double min, max, step;
    private double value;

    public NumberSetting(String name, double def, double min, double max, double step) {
        super(name);
        this.min = min;
        this.max = max;
        this.step = step;
        set(def);
    }

    public double get() {
        return value;
    }

    public float getF() {
        return (float) value;
    }

    public int getI() {
        return (int) Math.round(value);
    }

    public void set(double v) {
        double snapped = Math.round((v - min) / step) * step + min;
        value = Math.round(Math.max(min, Math.min(max, snapped)) * 1000.0) / 1000.0;
    }

    public String display() {
        return step >= 1 ? String.valueOf((int) Math.round(value)) : String.format(Locale.ROOT, "%.1f", value);
    }

    @Override
    public JsonElement save() {
        return new JsonPrimitive(value);
    }

    @Override
    public void load(JsonElement e) {
        try {
            if (e != null && e.isJsonPrimitive()) set(e.getAsDouble());
        } catch (Exception ignored) {
        }
    }
}
