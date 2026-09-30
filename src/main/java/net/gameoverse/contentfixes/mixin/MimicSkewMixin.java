package net.gameoverse.contentfixes.mixin;

import artifacts.entity.MimicEntity;
import net.gameoverse.contentfixes.Mimics;
import net.minecraft.core.Direction;
import net.minecraft.world.level.storage.ValueInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A dormant Mimic sits a little crooked (campsite ones too). setFacing covers new Mimics (dormant is set first);
 * loading sets the facing before the dormant flag, so the end of readAdditionalSaveData covers saved ones.
 */
@Mixin(MimicEntity.class)
public abstract class MimicSkewMixin {
    @Inject(method = "setFacing", at = @At("TAIL"))
    private void gameoverse$skew(Direction facing, CallbackInfo ci) {
        Mimics.skew((MimicEntity) (Object) this);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void gameoverse$skewLoaded(ValueInput input, CallbackInfo ci) {
        Mimics.skew((MimicEntity) (Object) this);
    }
}
