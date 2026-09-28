package net.gameoverse.contentfixes.mixin;

import com.sakuraryoko.afkplus.impl.player.AfkPlayer;
import net.gameoverse.contentfixes.AfkImmunity;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** See AfkImmunity: no AFK damage immunity before a real idle of AfkPlus's AFK timeout. */
@Mixin(targets = "com.sakuraryoko.afkplus.impl.player.AfkHandler", remap = false)
public abstract class AfkHandlerMixin {
    @Shadow
    private AfkPlayer player;

    @Inject(method = "disableDamage", at = @At("HEAD"), cancellable = true)
    private void gameoverse$onlyAfterRealIdle(CallbackInfo ci) {
        ServerPlayer serverPlayer = this.player.getPlayer();
        if (serverPlayer != null && !AfkImmunity.idleLongEnough(serverPlayer)) {
            ci.cancel();
        }
    }
}
