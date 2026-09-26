package net.gameoverse.contentfixes.mixin;

import com.cgessinger.creaturesandbeasts.CreaturesAndBeasts;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Triple;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Creatures and Beasts' Fabric port hooks its anvil recipes (Yeti Hide onto armor, Heal
 * Spell Book tier-up) at the third isEmpty() call in createResult, which on 26.1.2 sits in
 * the rename section, after vanilla has already returned for any right-hand item that isn't
 * a repair material or a matching damageable item. So neither combo ever produced a result.
 * The original mod fires its anvil event at the start; this calls the same logic there.
 */
@Mixin(AnvilMenu.class)
public abstract class CnbAnvilMixin extends ItemCombinerMenu {
    @Shadow @Final private DataSlot cost;
    @Shadow private int repairItemCountCost;

    protected CnbAnvilMixin(MenuType<?> type, int containerId, Inventory inventory,
                            ContainerLevelAccess access, ItemCombinerMenuSlotDefinition slots) {
        super(type, containerId, inventory, access, slots);
    }

    @Inject(method = "createResult", at = @At("HEAD"), cancellable = true)
    private void gameoverse$cnbAnvilRecipes(CallbackInfo ci) {
        ItemStack left = this.inputSlots.getItem(0);
        ItemStack right = this.inputSlots.getItem(1);
        if (left.isEmpty() || right.isEmpty()) {
            return;
        }
        Triple<Integer, Integer, ItemStack> result =
            CreaturesAndBeasts.getInstance().getEvents().onAnvilChange(left, right);
        if (result == null || result.getRight() == null || result.getRight().isEmpty()) {
            return;
        }
        this.cost.set(result.getLeft());
        this.repairItemCountCost = result.getMiddle();
        this.resultSlots.setItem(0, result.getRight());
        this.broadcastChanges();
        ci.cancel();
    }
}
