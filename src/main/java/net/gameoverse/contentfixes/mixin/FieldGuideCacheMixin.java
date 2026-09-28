package net.gameoverse.contentfixes.mixin;

import java.util.List;
import java.util.Map;

import com.evandev.fieldguide.client.manager.ClientCacheManager;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Field Guide 1.7.11 syncs every entry's loot to the client on join, 100 entries per packet,
 * and for each packet re-reads its whole on-disk cache (fieldguide_cache/&lt;session&gt;/drops.nbt,
 * 4.2 MB here), merges and rewrites it on the render thread: ~9 s of the join spent on disk.
 * The client keeps all of that loot in memory anyway, and anything missing there is requested
 * from the server, so the file only duplicates what the join already delivered. Skip both
 * the write and the read.
 */
@Mixin(value = ClientCacheManager.class, remap = false)
public abstract class FieldGuideCacheMixin {
    @Inject(method = "saveAllDrops", at = @At("HEAD"), cancellable = true)
    private static void gameoverse$skipSave(Map<Identifier, List<ItemStack>> drops, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "loadDrops", at = @At("HEAD"), cancellable = true)
    private static void gameoverse$skipLoad(Identifier id, CallbackInfoReturnable<List<ItemStack>> cir) {
        cir.setReturnValue(null);
    }
}
