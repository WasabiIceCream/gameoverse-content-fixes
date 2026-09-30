package net.gameoverse.contentfixes.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import fuzs.sneakycurses.common.client.handler.ItemTooltipHandler;

/**
 * Sneaky Curses hides an unrevealed curse by looking at each tooltip line's own contents: a
 * translatable {@code enchantment.<ns>.<path>} key becomes rune text, a longer key (a description,
 * {@code enchantment.<ns>.<path>.desc}) is removed. Dynamic Tooltips and Item Tooltips build their
 * description lines as {@code Component.literal("").append(...)}, so the key sits in a child and
 * the description of a hidden curse showed. When a line's own contents aren't an enchantment key,
 * hand Sneaky Curses the first child that is an enchantment description key instead. Only
 * description keys (more than three dot parts) are substituted, so a wrapped name line is never
 * replaced with runes by this; name lines are plain translatable components anyway.
 */
@Mixin(value = ItemTooltipHandler.class, remap = false)
public abstract class SneakyCurseDescriptionMixin {

    @WrapOperation(method = "onItemTooltip", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/network/chat/Component;getContents()Lnet/minecraft/network/chat/ComponentContents;"))
    private static ComponentContents gameoverse$findWrappedDescription(Component line, Operation<ComponentContents> original) {
        ComponentContents contents = original.call(line);
        if (isEnchantmentKey(contents)) {
            return contents;
        }
        ComponentContents found = findDescription(line, 0);
        return found != null ? found : contents;
    }

    private static ComponentContents findDescription(Component component, int depth) {
        for (Component sibling : component.getSiblings()) {
            ComponentContents contents = sibling.getContents();
            if (isEnchantmentKey(contents) && ((TranslatableContents) contents).getKey().split("\\.").length > 3) {
                return contents;
            }
            if (depth < 2) {
                ComponentContents nested = findDescription(sibling, depth + 1);
                if (nested != null) {
                    return nested;
                }
            }
        }
        return null;
    }

    private static boolean isEnchantmentKey(ComponentContents contents) {
        return contents instanceof TranslatableContents translatable && translatable.getKey().startsWith("enchantment.");
    }
}
