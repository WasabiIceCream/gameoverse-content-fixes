package net.gameoverse.contentfixes.mixin;

import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.FishingRodItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Casting and reeling a fishing rod don't count as activity. Vanilla resets a player's idle time on
 * every item use and arm swing, and AfkPlus uses that idle time, so an auto-fishing client (which
 * only casts and reels) was never marked AFK: no damage immunity, no AFK fishing slowdown
 * (AfkFishing), never kicked. Moving, looking around (AfkPlus resetOnLook) or any other action still
 * counts, so a person fishing by hand stays active.
 *
 * Chatting and typing commands don't count either, so players can talk or use /afk and other
 * commands without leaving AFK. Vanilla resets idle time for chat and for both kinds of command in
 * tryHandleChat; that reset is dropped.
 */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class RodActivityMixin {
    @Shadow
    public ServerPlayer player;

    @Redirect(method = "handleUseItem", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/server/level/ServerPlayer;resetLastActionTime()V"))
    private void gameoverse$rodUseIsNotActivity(ServerPlayer player, ServerboundUseItemPacket packet) {
        if (!(player.getItemInHand(packet.getHand()).getItem() instanceof FishingRodItem)) {
            player.resetLastActionTime();
        }
    }

    @Redirect(method = "handleAnimate", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/server/level/ServerPlayer;resetLastActionTime()V"))
    private void gameoverse$rodSwingIsNotActivity(ServerPlayer player, ServerboundSwingPacket packet) {
        InteractionHand hand = packet.getHand();
        if (!(player.getItemInHand(hand).getItem() instanceof FishingRodItem)) {
            player.resetLastActionTime();
        }
    }

    @Redirect(method = "tryHandleChat", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/server/level/ServerPlayer;resetLastActionTime()V"))
    private void gameoverse$chatIsNotActivity(ServerPlayer player, String message, boolean flag, Runnable handler) {
    }
}
