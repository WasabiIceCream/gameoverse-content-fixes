package net.gameoverse.contentfixes.mixin;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Bits and Balance 2.4.0's quick harvest (right-click a mature crop) builds its drops by running
 * the block's loot table directly, which skips any Block#getDrops override. Mystical
 * Agriculture's crops have no loot table and drop essence and seeds from that override, so a
 * quick harvest gave nothing but still replanted. Block.getDrops passes the same parameters
 * (state, origin, tool, entity, block entity) through the block, so loot-table crops drop
 * exactly as before.
 */
@Mixin(targets = "org.onenonly.bitsandbalance.common.mechanics.QuickHarvestingLogic", remap = false)
public abstract class QuickHarvestDropsMixin {
    @Inject(method = "collectDrops", at = @At("HEAD"), cancellable = true)
    private static void gameoverse$dropsThroughBlock(Block block, BlockState state, ServerLevel level, BlockPos pos,
            BlockEntity blockEntity, ServerPlayer player, ItemStack tool, CallbackInfoReturnable<List<ItemStack>> cir) {
        cir.setReturnValue(Block.getDrops(state, level, pos, blockEntity, player, tool));
    }
}
