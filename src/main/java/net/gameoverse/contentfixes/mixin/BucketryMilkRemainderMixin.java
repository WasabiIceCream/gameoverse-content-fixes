package net.gameoverse.contentfixes.mixin;

import java.util.function.Supplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Bucketry's milk buckets have no crafting remainder, so a cake ate the whole bucket.
 * Leave the empty bucket behind like vanilla milk does, keeping its wear.
 */
@Mixin(targets = "com.bucketsupdate.fabric.BaseMilkBucketItem", remap = false)
public abstract class BucketryMilkRemainderMixin {
    @Shadow
    @Final
    protected Supplier<? extends Item> emptyCounterpart;

    @Shadow
    protected abstract void copyState(ItemStack from, ItemStack to);

    public ItemStackTemplate getCraftingRemainder(ItemStack stack) {
        ItemStack empty = new ItemStack(emptyCounterpart.get());
        copyState(stack, empty);
        return ItemStackTemplate.fromNonEmptyStack(empty);
    }
}
