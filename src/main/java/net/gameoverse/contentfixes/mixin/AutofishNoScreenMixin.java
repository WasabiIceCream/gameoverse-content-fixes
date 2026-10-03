package net.gameoverse.contentfixes.mixin;

import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import troy.autofish.FabricModAutofish;
import troy.autofish.gui.AutofishScreenBuilder;

/**
 * The other way into XPlus Autofish's settings is Mod Menu's Configure button: the screen builder
 * hands back the screen it was opened from, so the button does nothing (see AutofishNoKeyMixin).
 */
@Mixin(value = AutofishScreenBuilder.class, remap = false)
public abstract class AutofishNoScreenMixin {
    @Inject(method = "buildScreen", at = @At("HEAD"), cancellable = true)
    private static void gameoverse$noSettings(FabricModAutofish mod, Screen parent, CallbackInfoReturnable<Screen> cir) {
        cir.setReturnValue(parent);
    }
}
