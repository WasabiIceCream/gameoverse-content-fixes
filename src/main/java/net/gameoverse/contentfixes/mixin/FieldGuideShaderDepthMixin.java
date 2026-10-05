package net.gameoverse.contentfixes.mixin;

import com.evandev.fieldguide.client.render.AfterLevelOverlay;
import com.evandev.fieldguide.client.render.DiscoveryOverlayRenderer;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.textures.GpuTexture;
import net.gameoverse.contentfixes.IrisShadowPass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Field Guide 1.21.0 clears the main depth buffer before drawing its scan and discovery overlays (the discovery
 * highlights run whenever something undiscovered is near). Eclipse reads that depth afterwards, so its volumetric clouds
 * and fog were drawn over the terrain, indoors too. With a shader pack on, skip the clear: the overlays then sit behind
 * walls instead of showing through them. Without one, nothing changes.
 */
@Mixin(value = {AfterLevelOverlay.class, DiscoveryOverlayRenderer.class}, remap = false)
public abstract class FieldGuideShaderDepthMixin {
    @WrapWithCondition(method = "render", at = @At(value = "INVOKE",
        target = "Lcom/mojang/blaze3d/systems/CommandEncoder;clearDepthTexture(Lcom/mojang/blaze3d/textures/GpuTexture;D)V"))
    private static boolean gameoverse$keepDepthForShaders(CommandEncoder encoder, GpuTexture texture, double depth) {
        return !IrisShadowPass.shaderPackInUse();
    }
}
