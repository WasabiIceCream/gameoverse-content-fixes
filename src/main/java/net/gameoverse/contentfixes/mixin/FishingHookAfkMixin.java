package net.gameoverse.contentfixes.mixin;

import com.sakuraryoko.afkplus.api.AfkPlusAPI;
import net.gameoverse.contentfixes.AfkFishing;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.FishingHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** See AfkFishing: while the owner is AFK, fishing progresses only every Nth tick. */
@Mixin(FishingHook.class)
public abstract class FishingHookAfkMixin {
    @Inject(method = "catchingFish", at = @At("HEAD"), cancellable = true)
    private void gameoverse$slowAfkFishing(BlockPos pos, CallbackInfo ci) {
        int slowdown = AfkFishing.slowdown();
        if (slowdown <= 1) return;
        FishingHook hook = (FishingHook) (Object) this;
        if (hook.getPlayerOwner() instanceof ServerPlayer player
                && AfkPlusAPI.isPlayerAfk(player)
                && hook.tickCount % slowdown != 0) {
            ci.cancel();
        }
    }
}
