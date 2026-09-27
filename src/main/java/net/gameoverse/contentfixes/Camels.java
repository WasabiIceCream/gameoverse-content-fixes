package net.gameoverse.contentfixes;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings;

/**
 * Camels were close to unfindable: No Villages removes the desert village camel pens, the
 * only reliable source, which leaves the natural spawn at weight 1 (against rabbits at 12).
 * This raises it to {@link #WEIGHT} wherever camels already spawn and adds them to Regions
 * Unexplored's two sand deserts, which had none. Camels only spawn on
 * {@code #minecraft:camels_spawnable_on} (sand), so grassy biomes in the list rarely get one.
 */
final class Camels {
    static final int WEIGHT = 3;

    private Camels() {
    }

    private static ResourceKey<Biome> ru(String path) {
        return ResourceKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath("regions_unexplored", path));
    }

    static void register() {
        BiomeModifications.create(Identifier.fromNamespaceAndPath("gameoverse_content_fixes", "camels"))
            .add(ModificationPhase.POST_PROCESSING, BiomeSelectors.includeByKey(Biomes.DESERT,
                    ru("baobab_savanna"), ru("dry_bushland"), ru("steppe"),
                    ru("saguaro_desert"), ru("joshua_desert")), context -> {
                context.getMobSpawnSettings().removeSpawnsOfEntityType(EntityType.CAMEL);
                context.getMobSpawnSettings().addSpawn(MobCategory.CREATURE,
                    new MobSpawnSettings.SpawnerData(EntityType.CAMEL, 1, 1), WEIGHT);
            });
    }
}
