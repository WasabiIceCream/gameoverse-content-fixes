package net.gameoverse.contentfixes.mixin;

import net.gameoverse.contentfixes.MimicDirection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/** Exposes the Mimic's turn-to direction (the controller class is protected). See {@code Mimics.skew}. */
@Mixin(targets = "artifacts.entity.MimicEntity$MimicMovementController")
public abstract class MimicControllerMixin implements MimicDirection {
    @Shadow
    public abstract void setDirection(float yaw, boolean jump);

    @Override
    public void gameoverse$setDirection(float yaw) {
        setDirection(yaw, false);
    }
}
