package net.gameoverse.contentfixes.mixin;

import java.util.ArrayList;
import java.util.List;
import net.gameoverse.contentfixes.Invokers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Inside a structure whose own monster spawn list includes pillagers (every pillager outpost
 * variant, the illager fort and similar), adds the Invoker at a small weight. mobsAt is also
 * what canSpawnMobAt re-checks a picked spawn against, so the added entry stays consistent.
 * See {@link Invokers}.
 */
@Mixin(NaturalSpawner.class)
public abstract class NaturalSpawnerInvokerMixin {
    @Inject(method = "mobsAt", at = @At("RETURN"), cancellable = true)
    private static void gameoverse$addInvoker(ServerLevel level, StructureManager structures, ChunkGenerator generator,
                                              MobCategory category, BlockPos pos, Holder<Biome> biome,
                                              CallbackInfoReturnable<WeightedList<MobSpawnSettings.SpawnerData>> cir) {
        if (category != MobCategory.MONSTER) {
            return;
        }
        WeightedList<MobSpawnSettings.SpawnerData> list = cir.getReturnValue();
        List<Weighted<MobSpawnSettings.SpawnerData>> entries = list.unwrap();
        if (entries.stream().noneMatch(e -> e.value().type() == EntityType.PILLAGER)) {
            return;
        }
        EntityType<?> invoker = Invokers.invokerType();
        if (invoker == null || entries.stream().anyMatch(e -> e.value().type() == invoker)) {
            return;
        }
        List<Weighted<MobSpawnSettings.SpawnerData>> scaled = new ArrayList<>(entries.size() + 1);
        for (Weighted<MobSpawnSettings.SpawnerData> e : entries) {
            scaled.add(new Weighted<>(e.value(), e.weight() * Invokers.STRUCTURE_WEIGHT_SCALE));
        }
        scaled.add(new Weighted<>(new MobSpawnSettings.SpawnerData(invoker, 1, 1), 1));
        cir.setReturnValue(WeightedList.of(scaled));
    }
}
