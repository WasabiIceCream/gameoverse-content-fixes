package net.gameoverse.contentfixes.mixin;

import java.util.ArrayList;
import java.util.List;
import net.gameoverse.contentfixes.LootContainerProtection;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ServerExplosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Explosions skip loot containers SlashLoot handles (see {@link LootContainerProtection}).
 * Filters the list handed to {@code interactWithBlocks}, not {@code calculateExplodedPositions}'s
 * return: Lithium replaces that method from its head and returns early, which skips RETURN hooks.
 */
@Mixin(ServerExplosion.class)
public abstract class ExplosionLootContainerMixin {
    @Shadow
    public abstract ServerLevel level();

    @ModifyVariable(method = "interactWithBlocks", at = @At("HEAD"), argsOnly = true)
    private List<BlockPos> gameoverse$skipLootContainers(List<BlockPos> positions) {
        ServerLevel level = level();
        if (positions.stream().noneMatch(pos -> LootContainerProtection.isLootContainer(level.getBlockEntity(pos)))) {
            return positions;
        }
        List<BlockPos> kept = new ArrayList<>(positions.size());
        for (BlockPos pos : positions) {
            if (!LootContainerProtection.isLootContainer(level.getBlockEntity(pos))) {
                kept.add(pos);
            }
        }
        return kept;
    }
}
