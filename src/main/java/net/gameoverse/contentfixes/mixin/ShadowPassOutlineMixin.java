package net.gameoverse.contentfixes.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.gameoverse.contentfixes.IrisShadowPass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Iris's shadow pass extracts entity render states itself, glowing outlines included, and the
 * outline vertices it writes there are never drawn or flushed, so they pile up frame after frame
 * until BufferBuilder throws "Trying to write too many vertices". Better Item Despawn makes every
 * dropped item nearby glow, so a pile of drops crashed a shader user within half a minute
 * (2026-09-28). An outline has no place in a shadow map: report "not glowing" during that pass
 * only; the normal pass still draws the glow.
 */
@Mixin(EntityRenderer.class)
public abstract class ShadowPassOutlineMixin {
    @WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/Minecraft;shouldEntityAppearGlowing(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean gameoverse$noShadowOutline(Minecraft minecraft, Entity entity, Operation<Boolean> original) {
        return !IrisShadowPass.active() && original.call(minecraft, entity);
    }
}
