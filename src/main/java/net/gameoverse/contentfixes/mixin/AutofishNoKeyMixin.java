package net.gameoverse.contentfixes.mixin;

import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import troy.autofish.FabricModAutofish;

/**
 * XPlus Autofish's settings are the server's (config/autofish-client.toml is force-synced by
 * AutoModpack): its Auto Turn View, for one, counts as looking around, which keeps an auto-fisher
 * from ever going AFK. Its settings key (V) is never registered, so it isn't in Controls and
 * can't be bound; the unregistered mapping is never pressed, so Autofish's own check stays false.
 */
@Mixin(value = FabricModAutofish.class, remap = false)
public abstract class AutofishNoKeyMixin {
    @Redirect(method = "onInitializeClient", at = @At(value = "INVOKE",
        target = "Lnet/fabricmc/fabric/api/client/keymapping/v1/KeyMappingHelper;registerKeyMapping(Lnet/minecraft/client/KeyMapping;)Lnet/minecraft/client/KeyMapping;"))
    private KeyMapping gameoverse$dontRegister(KeyMapping mapping) {
        return mapping;
    }
}
