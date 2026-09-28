package net.gameoverse.contentfixes.mixin;

import net.gameoverse.contentfixes.LootPlayerContext;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Apotheosis only adds gems and converts gear to affixed gear when GenContext.findPlayer finds a
 * player in the loot context (this entity or an attacker). Fishing passes the bobber as the
 * entity and Go Fish crates pass no entity, so neither ever got gems or affixes. Fall back to the
 * bobber's owner, then to the player opening a crate.
 */
@Mixin(targets = "dev.shadowsoffire.apotheosis.tiers.GenContext", remap = false)
public abstract class ApotheosisLootPlayerMixin {
    @Inject(method = "findPlayer", at = @At("RETURN"), cancellable = true)
    private static void gameoverse$fishingAndCratePlayer(LootContext context, CallbackInfoReturnable<Player> cir) {
        if (cir.getReturnValue() != null) return;
        Entity entity = context.getOptionalParameter(LootContextParams.THIS_ENTITY);
        if (entity instanceof FishingHook hook && hook.getPlayerOwner() != null) {
            cir.setReturnValue(hook.getPlayerOwner());
            return;
        }
        Player opener = LootPlayerContext.CRATE_OPENER.get();
        if (opener != null) cir.setReturnValue(opener);
    }
}
