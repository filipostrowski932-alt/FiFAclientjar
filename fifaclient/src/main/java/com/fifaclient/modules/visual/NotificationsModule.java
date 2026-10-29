package com.fifaclient.modules.visual;

import com.fifaclient.core.Category;
import com.fifaclient.core.ClientModule;
import com.fifaclient.core.setting.NumberSetting;

public class NotificationsModule extends ClientModule {
    public static NotificationsModule INSTANCE;

    public final NumberSetting duration = add(new NumberSetting("Duration (s)", 2.5, 1.0, 6.0, 0.5));

    public NotificationsModule() {
        super("Notifications", "Slide-in toasts when you toggle modules.", Category.VISUAL, true);
        this.listed = false;
        INSTANCE = this;
    }

    public static boolean shouldShow() {
        return INSTANCE != null && INSTANCE.isEnabled();
    }

    public static long durationMs() {
        return INSTANCE == null ? 2500L : (long) (INSTANCE.duration.get() * 1000.0);
    }
}
