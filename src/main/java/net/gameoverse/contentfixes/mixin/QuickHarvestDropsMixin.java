package net.gameoverse.contentfixes.mixin;

import java.util.List;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Three fixes to Bits and Balance 2.4.0's quick harvest (right-click a mature crop).
 *
 * Drops: it builds drops by running the block's loot table directly, which skips any
 * Block#getDrops override. Mystical Agriculture's crops have no loot table and drop essence and
 * seeds from that override, so a quick harvest gave nothing but still replanted. Block.getDrops
 * passes the same parameters (state, origin, tool, entity, block entity) through the block, so
 * loot-table crops drop exactly as before.
 *
 * Off-hand: it decides per hand, and an empty hand counts as a harvest. Holding right-click with
 * bone meal or fertilizer, the game retries the use; once the crop matures the main-hand item no
 * longer applies, the game falls through to an empty off-hand, and the crop was harvested on the
 * same press. Only quick-harvest from the off-hand when the main hand is empty too.
 *
 * Break event: it destroys the block through the level, never as a player break, so nothing
 * listening to Fabric's PlayerBlockBreakEvents.AFTER saw the harvest (Skill Tree and Skill
 * Proficiencies XP, Heart Crystal and Rumor crop drops). Fire AFTER once a harvest succeeds.
 */
@Mixin(targets = "org.onenonly.bitsandbalance.common.mechanics.QuickHarvestingLogic", remap = false)
public abstract class QuickHarvestDropsMixin {
    @Inject(method = "collectDrops", at = @At("HEAD"), cancellable = true)
    private static void gameoverse$dropsThroughBlock(Block block, BlockState state, ServerLevel level, BlockPos pos,
            BlockEntity blockEntity, ServerPlayer player, ItemStack tool, CallbackInfoReturnable<List<ItemStack>> cir) {
        cir.setReturnValue(Block.getDrops(state, level, pos, blockEntity, player, tool));
    }

    @Inject(method = "tryHandleInteraction", at = @At("HEAD"), cancellable = true)
    private static void gameoverse$noOffHandFallthrough(ServerPlayer player, Level level, InteractionHand hand,
            BlockPos pos, BlockState state, boolean hoeEnabled, boolean axeEnabled, boolean flag,
            CallbackInfoReturnable<Boolean> cir) {
        if (hand == InteractionHand.OFF_HAND && !player.getMainHandItem().isEmpty()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "harvestTarget", at = @At("RETURN"))
    private static void gameoverse$fireBreakEvent(ServerPlayer player, ServerLevel level, BlockPos pos, BlockState state,
            ItemStack tool, EquipmentSlot slot, @Coerce Object action,
            boolean flag, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) {
            PlayerBlockBreakEvents.AFTER.invoker().afterBlockBreak(level, player, pos, state, null);
        }
    }
}
