package com.fifaclient.core;

import com.fifaclient.FifaClient;
import com.fifaclient.core.setting.Setting;
import com.fifaclient.modules.hud.HudModule;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/** Saves/loads everything to .minecraft/config/fifaclient.json */
public final class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private Config() {
    }

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("fifaclient.json");
    }

    public static void load() {
        Path p = path();
        if (!Files.exists(p)) return;
        try (Reader reader = Files.newBufferedReader(p)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            for (ClientModule m : ModuleManager.all()) {
                if (!root.has(m.name)) continue;
                JsonObject o = root.getAsJsonObject(m.name);
                if (o.has("enabled")) m.loadEnabled(o.get("enabled").getAsBoolean());
                if (m instanceof HudModule h) {
                    if (o.has("x")) h.x = o.get("x").getAsFloat();
                    if (o.has("y")) h.y = o.get("y").getAsFloat();
                }
                if (o.has("settings")) {
                    JsonObject so = o.getAsJsonObject("settings");
                    for (Setting s : m.settings) {
                        if (so.has(s.name)) s.load(so.get(s.name));
                    }
                }
            }
        } catch (Exception e) {
            FifaClient.LOGGER.warn("Could not read fifaclient.json, using defaults", e);
        }
    }

    public static void save() {
        JsonObject root = new JsonObject();
        for (ClientModule m : ModuleManager.all()) {
            JsonObject o = new JsonObject();
            o.addProperty("enabled", m.isEnabled());
            if (m instanceof HudModule h && !Float.isNaN(h.x) && !Float.isNaN(h.y)) {
                o.addProperty("x", h.x);
                o.addProperty("y", h.y);
            }
            JsonObject so = new JsonObject();
            for (Setting s : m.settings) so.add(s.name, s.save());
            o.add("settings", so);
            root.add(m.name, o);
        }
        try (Writer w = Files.newBufferedWriter(path())) {
            GSON.toJson(root, w);
        } catch (Exception e) {
            FifaClient.LOGGER.warn("Could not save fifaclient.json", e);
        }
    }
}
