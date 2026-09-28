package net.gameoverse.contentfixes.mixin;

import dev.muon.dynamic_difficulty.api.LevelingAPI;
import dev.muon.dynamic_difficulty.util.LevelingUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Dynamic Difficulty 1.3.3 draws a level plate (and Jade line) for every mob not in the client's
 * hiddenLevelEntities list; one with no level reads as "Level 1". The server only syncs levels
 * for mobs that have one (plus players), so on the client a mob without the level attachment has
 * no level: hide its plate. Covers passive mobs and anything else the server doesn't level,
 * without keeping a second list in the client config.
 */
@Mixin(value = LevelingUtils.class, remap = false)
public abstract class DifficultyLevelPlateMixin {
    @Inject(method = "shouldShowLevel", at = @At("HEAD"), cancellable = true)
    private static void gameoverse$hideUnleveled(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (!(entity instanceof Player) && !LevelingAPI.hasLevel(entity)) {
            cir.setReturnValue(false);
        }
    }
}
