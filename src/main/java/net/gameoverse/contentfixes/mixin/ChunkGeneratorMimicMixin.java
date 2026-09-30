package net.gameoverse.contentfixes.mixin;

import net.gameoverse.contentfixes.Mimics;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Once a chunk's structures and features are placed, some of its loot chests become Mimics. See {@link Mimics}. */
@Mixin(ChunkGenerator.class)
public abstract class ChunkGeneratorMimicMixin {
    @Inject(method = "applyBiomeDecoration", at = @At("TAIL"))
    private void gameoverse$mimics(WorldGenLevel level, ChunkAccess chunk, StructureManager structures, CallbackInfo ci) {
        Mimics.replaceChests(level, chunk);
    }
}
