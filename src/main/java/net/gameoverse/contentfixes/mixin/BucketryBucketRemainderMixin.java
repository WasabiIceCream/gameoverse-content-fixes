package net.gameoverse.contentfixes.mixin;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Bucketry's filled buckets leave a fresh empty bucket when used in a recipe, which
 * would repair a worn wooden or bamboo bucket for free. Leave the worn one instead.
 */
@Mixin(targets = "com.bucketsupdate.fabric.BaseBucketItem", remap = false)
public abstract class BucketryBucketRemainderMixin {
    @Shadow
    public abstract boolean isEmpty();

    @Shadow
    public abstract ItemStack toEmpty(ItemStack stack);

    public ItemStackTemplate getCraftingRemainder(ItemStack stack) {
        if (isEmpty()) {
            return ((Item) (Object) this).getCraftingRemainder();
        }
        return ItemStackTemplate.fromNonEmptyStack(toEmpty(stack));
    }
}
