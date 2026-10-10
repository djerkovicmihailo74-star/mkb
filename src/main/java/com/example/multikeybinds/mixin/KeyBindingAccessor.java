package com.example.multikeybinds.mixin;

import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(KeyBinding.class)
public interface KeyBindingAccessor {
    @Accessor("timesPressed")
    int mkb$getTimesPressed();

    @Accessor("timesPressed")
    void mkb$setTimesPressed(int value);
}
