package com.fifaclient.mixin;

import net.minecraft.client.option.SimpleOption;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Lets Zoom set the FOV option below vanilla's slider minimum (30)
 * without touching validation. Used only for the temporary zoom value.
 */
@Mixin(SimpleOption.class)
public interface SimpleOptionAccessor<T> {
    @Accessor("value")
    void fifaclient$setRaw(T value);
}
