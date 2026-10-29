package com.fifaclient;

import com.fifaclient.core.Config;
import com.fifaclient.core.ModuleManager;
import com.fifaclient.gui.ClickGuiScreen;
import com.fifaclient.gui.HudEditorScreen;
import com.fifaclient.modules.hud.HudModule;
import com.fifaclient.render.Draw;
import com.fifaclient.render.Notifications;
import com.fifaclient.render.TitleBrand;
import com.fifaclient.util.Cps;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FifaClient implements ClientModInitializer {
    public static final String MOD_ID = "fifaclient";
    public static final String VERSION = "1.0.0";
    public static final Logger LOGGER = LoggerFactory.getLogger("FIFACLIENT");

    public static KeyBinding openGuiKey;
    public static KeyBinding zoomKey;

    @Override
    public void onInitializeClient() {
        KeyBinding.Category category = KeyBinding.Category.create(Identifier.of(MOD_ID, "main"));
        openGuiKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.fifaclient.open_gui", GLFW.GLFW_KEY_RIGHT_SHIFT, category));
        zoomKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.fifaclient.zoom", GLFW.GLFW_KEY_C, category));

        ModuleManager.init();
        Config.load();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGuiKey.wasPressed()) {
                if (client.currentScreen == null) client.setScreen(new ClickGuiScreen());
            }
            ModuleManager.tick(client);
        });

        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT,
                Identifier.of(MOD_ID, "hud"),
                (context, tickCounter) -> renderHud(context));

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof TitleScreen) {
                ScreenEvents.afterRender(screen).register(
                        (s, context, mouseX, mouseY, tickDelta) -> TitleBrand.render(context));
            }
        });

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            ModuleManager.shutdown(client);
            Config.save();
        });

        LOGGER.info("FIFACLIENT {} loaded", VERSION);
    }

    private static void renderHud(DrawContext context) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        Draw.alphaMul = 1f;
        Cps.poll(mc);
        ModuleManager.frame(mc);

        // the HUD editor draws the elements itself so they are not doubled up
        if (!(mc.currentScreen instanceof HudEditorScreen)) {
            for (HudModule m : ModuleManager.huds()) {
                if (m.isEnabled()) m.draw(context, mc);
            }
        }
        Notifications.render(context, mc);
    }
}
