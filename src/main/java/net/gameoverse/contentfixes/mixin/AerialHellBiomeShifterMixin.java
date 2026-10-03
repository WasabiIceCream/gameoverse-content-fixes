package net.gameoverse.contentfixes.mixin;

import java.util.Optional;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Aerial Hell's Biome Shifter block entity reads {@code field_size} with {@code getInt(..).get()}. A block entity
 * data packet without that field (sent after a Mud Dungeon was cleared, 2026-10-03) threw on the client and
 * disconnected the player ("Network Protocol Error"). Missing now keeps the current size. Its own mixin config is
 * optional, so nothing breaks without Aerial Hell.
 */
@Mixin(targets = "fr.factionbedrock.aerialhell.BlockEntity.BiomeShifterBlockEntity", remap = false)
public abstract class AerialHellBiomeShifterMixin {
    @Shadow
    private int fieldSize;

    @Redirect(method = "loadAdditional", at = @At(value = "INVOKE", target = "Ljava/util/Optional;get()Ljava/lang/Object;"))
    private Object gameoverse$keepSizeWhenMissing(Optional<Object> value) {
        return value.orElse(this.fieldSize);
    }
}
