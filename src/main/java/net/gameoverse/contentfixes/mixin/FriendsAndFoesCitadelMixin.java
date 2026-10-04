package net.gameoverse.contentfixes.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Friends and Foes only places a Citadel where 25 columns over 32x32 blocks are open (air or fluid) at every
 * 5th block for 53 blocks above the start. Our Nether terrain (Incendium, Regions Unexplored) passes that at
 * about 3.5% of candidates, so no Citadel generated in ~15,000 Nether chunks (2026-10-03 audit). This accepts
 * 90% open samples (about 15% of candidates); the structure overwrites the little rock left inside it. Its own
 * mixin config is optional, so nothing breaks without Friends and Foes.
 */
@Mixin(targets = "com.faboslav.friendsandfoes.common.world.structures.CitadelStructure", remap = false)
public abstract class FriendsAndFoesCitadelMixin {
    private static final int SAMPLES = 25 * 11;
    private static final int REQUIRED_OPEN = (int) Math.ceil(SAMPLES * 0.9);

    @Inject(method = "extraSpawningChecks", at = @At("HEAD"), cancellable = true)
    private void gameoverse$mostlyOpenSpace(Structure.GenerationContext context, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        int open = 0;
        for (int dx = -16; dx <= 16; dx += 8) {
            for (int dz = -16; dz <= 16; dz += 8) {
                NoiseColumn column = context.chunkGenerator().getBaseColumn(pos.getX() + dx, pos.getZ() + dz,
                    context.heightAccessor(), context.randomState());
                for (int dy = 0; dy <= 53; dy += 5) {
                    BlockState state = column.getBlock(pos.getY() + dy);
                    if (state.isAir() || !state.getFluidState().isEmpty()) {
                        open++;
                    }
                }
            }
        }
        cir.setReturnValue(open >= REQUIRED_OPEN);
    }
}
