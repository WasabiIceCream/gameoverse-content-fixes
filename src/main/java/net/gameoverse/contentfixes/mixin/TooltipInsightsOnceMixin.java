package net.gameoverse.contentfixes.mixin;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Effect Insights (through its bundled Tooltip Insights library) adds an effect's description after every tooltip
 * line that names the effect. An Apotheosis Potion Charm names its effect four times (title, "Applies...",
 * "Source:" and the potion contents), so the same description showed four times. The handler asks
 * {@code map.containsKey(key)} once per occurrence, walking lines top to bottom; this map answers yes only for the
 * last occurrence, which for a potion-like item is its contents line, where vanilla potions show it.
 */
@Pseudo
@Mixin(targets = "fuzs.tooltipinsights.common.api.v1.client.handler.TooltipDescriptionsHandler", remap = false)
public abstract class TooltipInsightsOnceMixin {

    @ModifyExpressionValue(method = "modifyTooltip", require = 0, at = @At(value = "INVOKE",
            target = "Lfuzs/tooltipinsights/common/api/v1/client/handler/TooltipDescriptionsHandler;getByDescriptionId(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/core/HolderLookup$Provider;)Ljava/util/Map;"))
    private Map<String, Object> gameoverse$onlyLastOccurrence(Map<String, Object> byKey, @Local(argsOnly = true) List<Component> lines) {
        if (byKey.isEmpty()) {
            return byKey;
        }
        Map<String, Integer> remaining = new HashMap<>();
        for (Component line : lines) {
            count(line, byKey, remaining, 0);
        }
        return new LastOccurrenceMap(byKey, remaining);
    }

    private static void count(Component component, Map<String, Object> byKey, Map<String, Integer> counts, int depth) {
        if (depth > 16) return;
        if (component.getContents() instanceof TranslatableContents t) {
            if (byKey.containsKey(t.getKey())) {
                counts.merge(t.getKey(), 1, Integer::sum);
            }
            for (Object arg : t.getArgs()) {
                if (arg instanceof Component c) count(c, byKey, counts, depth + 1);
            }
        }
        for (Component sibling : component.getSiblings()) {
            count(sibling, byKey, counts, depth + 1);
        }
    }

    private static final class LastOccurrenceMap extends AbstractMap<String, Object> {
        private final Map<String, Object> delegate;
        private final Map<String, Integer> remaining;

        LastOccurrenceMap(Map<String, Object> delegate, Map<String, Integer> remaining) {
            this.delegate = delegate;
            this.remaining = remaining;
        }

        @Override
        public boolean containsKey(Object key) {
            if (!this.delegate.containsKey(key)) return false;
            Integer left = this.remaining.get(key);
            if (left == null || left <= 1) return true;
            this.remaining.put((String) key, left - 1);
            return false;
        }

        @Override
        public Object get(Object key) {
            return this.delegate.get(key);
        }

        @Override
        public boolean isEmpty() {
            return this.delegate.isEmpty();
        }

        @Override
        public Set<Entry<String, Object>> entrySet() {
            return this.delegate.entrySet();
        }
    }
}
