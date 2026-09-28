package net.gameoverse.contentfixes.mixin;

import net.gameoverse.contentfixes.LootPlayerContext;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Records who is opening a Go Fish crate while its loot rolls (synchronously, inside use). */
@Mixin(targets = "draylar.gofish.item.CrateItem", remap = false)
public abstract class GoFishCrateMixin {
    @Inject(method = "use", at = @At("HEAD"))
    private void gameoverse$rememberOpener(Level level, Player player, InteractionHand hand,
            CallbackInfoReturnable<InteractionResult> cir) {
        LootPlayerContext.CRATE_OPENER.set(player);
    }

    @Inject(method = "use", at = @At("RETURN"))
    private void gameoverse$forgetOpener(Level level, Player player, InteractionHand hand,
            CallbackInfoReturnable<InteractionResult> cir) {
        LootPlayerContext.CRATE_OPENER.remove();
    }
}
