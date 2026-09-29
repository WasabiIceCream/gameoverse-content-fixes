package net.gameoverse.contentfixes.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;

import net.skill_tree_rpgs.utils.ResolvableTextContent;

/**
 * Skill Tree's live skill descriptions ({@code {"skill_definition_id": ...}}, a text component type it adds) never
 * showed: text is sent to the client without its "type" field, the client guesses the type by trying each one in
 * order, and 26.1's {@code object} type (all fields optional) matches first, so the description arrives empty. Decode
 * anything carrying {@code skill_definition_id} as Skill Tree's type first.
 */
@Mixin(targets = "net.minecraft.network.chat.ComponentSerialization$FuzzyCodec")
public class SkillTreeDescriptionMixin {

    @Inject(method = "decode", at = @At("HEAD"), cancellable = true)
    private <S> void gameoverse$skillTreeDescription(DynamicOps<S> ops, MapLike<S> input, CallbackInfoReturnable<DataResult<?>> cir) {
        if (input.get("skill_definition_id") != null) {
            cir.setReturnValue(ResolvableTextContent.CODEC.decode(ops, input));
        }
    }
}
