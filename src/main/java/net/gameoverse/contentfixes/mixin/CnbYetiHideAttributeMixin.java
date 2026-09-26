package net.gameoverse.contentfixes.mixin;

import com.cgessinger.creaturesandbeasts.CreaturesAndBeasts;
import java.util.function.BiConsumer;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.Equippable;
import org.apache.commons.lang3.function.TriConsumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Creatures and Beasts stores Yeti Hides on armor as a HideAmount counter and adds the armor
 * bonus in CNBEvents.onItemAttributeModifierCalculate, but its Fabric port never calls that
 * method, so the hides did nothing. This calls it from both forEachModifier overloads: the
 * per-slot one (live stats) and the per-group one (tooltips), same pattern as
 * gameoverse-material-traits.
 */
@Mixin(ItemStack.class)
public abstract class CnbYetiHideAttributeMixin {
    @Inject(method = "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V", at = @At("TAIL"))
    private void gameoverse$yetiHideForSlot(EquipmentSlot slot, BiConsumer<Holder<Attribute>, AttributeModifier> sink, CallbackInfo ci) {
        CreaturesAndBeasts.getInstance().getEvents().onItemAttributeModifierCalculate((ItemStack) (Object) this, slot, sink);
    }

    @Inject(
        method = "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlotGroup;Lorg/apache/commons/lang3/function/TriConsumer;)V",
        at = @At("TAIL")
    )
    private void gameoverse$yetiHideForGroup(
        EquipmentSlotGroup group, TriConsumer<Holder<Attribute>, AttributeModifier, ItemAttributeModifiers.Display> sink, CallbackInfo ci
    ) {
        ItemStack self = (ItemStack) (Object) this;
        Equippable equippable = self.get(DataComponents.EQUIPPABLE);
        if (equippable == null || !group.test(equippable.slot())) {
            return;
        }
        CreaturesAndBeasts.getInstance().getEvents().onItemAttributeModifierCalculate(self, equippable.slot(),
            (attribute, modifier) -> sink.accept(attribute, modifier, ItemAttributeModifiers.Display.attributeModifiers()));
    }
}
