package net.gameoverse.contentfixes.mixin;

import artifacts.world.AbstractCampsiteFeature;
import net.minecraft.world.entity.EntitySpawnReason;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Respawning Animals cancels every mob that joins with the CHUNK_GENERATION spawn reason (its "world_gen" animals),
 * which is what Artifacts' campsites spawn their Mimics with, so no campsite ever had one. STRUCTURE is its
 * "scripted" reason, left alone. Same reason {@code Mimics.replaceChests} uses.
 */
@Mixin(AbstractCampsiteFeature.class)
public abstract class CampsiteMimicReasonMixin {
    @ModifyArg(method = "placeChest", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/EntityType;create(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/EntitySpawnReason;)Lnet/minecraft/world/entity/Entity;"),
        index = 1)
    private EntitySpawnReason gameoverse$structureReason(EntitySpawnReason reason) {
        return EntitySpawnReason.STRUCTURE;
    }
}
