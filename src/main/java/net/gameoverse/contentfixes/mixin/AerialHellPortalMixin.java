package net.gameoverse.contentfixes.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.portal.TeleportTransition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Aerial Hell is post-End content here (docs/post-end-progression.md), so its portal links the End and Aerial Hell
 * only. Upstream links Aerial Hell to the Overworld and lights anywhere: a player could leave into the Overworld,
 * and a portal built there let anyone in without reaching the End. Now leaving Aerial Hell goes to the End, and a
 * portal only lights (or does anything) in the End or Aerial Hell. The 1:1 coordinate scale that makes the pairs
 * line up is a dimension type override in gameoverse-aerial-hell-gate.
 */
@Mixin(targets = "fr.factionbedrock.aerialhell.Block.AerialHellPortalBlock", remap = false)
public abstract class AerialHellPortalMixin {
    @Unique
    private static final ResourceKey<Level> AERIAL_HELL =
        ResourceKey.create(Registries.DIMENSION, Identifier.fromNamespaceAndPath("aerialhell", "aerial_hell"));

    @Unique
    private static boolean gameoverse$linked(ResourceKey<Level> dimension) {
        return dimension == Level.END || dimension.equals(AERIAL_HELL);
    }

    @Inject(method = "getPortalDestination", at = @At("HEAD"), cancellable = true)
    private void gameoverse$onlyFromEndOrAerialHell(ServerLevel level, Entity entity, BlockPos pos, CallbackInfoReturnable<TeleportTransition> cir) {
        if (!gameoverse$linked(level.dimension())) {
            cir.setReturnValue(null);
        }
    }

    @ModifyVariable(method = "getPortalDestination", at = @At("STORE"), ordinal = 0)
    private ResourceKey<Level> gameoverse$exitToEnd(ResourceKey<Level> destination) {
        return destination == Level.OVERWORLD ? Level.END : destination;
    }

    @Inject(method = "trySpawnPortal", at = @At("HEAD"), cancellable = true)
    private void gameoverse$lightOnlyInEndOrAerialHell(LevelAccessor level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (level instanceof Level real && !gameoverse$linked(real.dimension())) {
            cir.setReturnValue(false);
        }
    }
}
