package com.fifaclient.modules.client;

import com.fifaclient.core.Category;
import com.fifaclient.core.ClientModule;
import com.fifaclient.core.setting.ModeSetting;
import com.fifaclient.core.setting.NumberSetting;
import com.fifaclient.render.Theme;

public class AppearanceModule extends ClientModule {
    public static AppearanceModule INSTANCE;

    public final ModeSetting theme = add(new ModeSetting("Theme", "FIFA", Theme.NAMES));
    public final NumberSetting dim = add(new NumberSetting("Background Dim", 0.45, 0.0, 0.85, 0.05));
    public final NumberSetting roundness = add(new NumberSetting("Roundness", 5, 0, 8, 1));

    public AppearanceModule() {
        super("Appearance", "Theme colours and the look of the FIFACLIENT interface.", Category.CLIENT, true);
        this.toggleable = false;
        this.listed = false;
        INSTANCE = this;
    }

    public static int themeIndex() {
        return INSTANCE == null ? 0 : INSTANCE.theme.index();
    }

    public static float dim() {
        return INSTANCE == null ? 0.45f : INSTANCE.dim.getF();
    }

    public static int round() {
        return INSTANCE == null ? 5 : INSTANCE.roundness.getI();
    }
}
