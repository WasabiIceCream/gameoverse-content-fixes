package net.gameoverse.contentfixes.mixin;

import net.gameoverse.contentfixes.LootContainerProtection;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Chest minecarts and chest boats with loot can't be damaged (see {@link LootContainerProtection}). */
@Mixin(VehicleEntity.class)
public abstract class VehicleLootContainerMixin {
    @Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
    private void gameoverse$protectLootVehicle(ServerLevel level, DamageSource source, float amount,
                                               CallbackInfoReturnable<Boolean> cir) {
        if (LootContainerProtection.protectsVehicle((VehicleEntity) (Object) this, source)) {
            cir.setReturnValue(false);
        }
    }
}
