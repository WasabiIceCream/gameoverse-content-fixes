package net.gameoverse.contentfixes;

import java.util.List;
import java.util.Set;

import dev.chybx.spideroverhaul.config.SpiderOverhaulConfig;
import dev.chybx.spideroverhaul.registry.ModEntities;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

/**
 * Gives a source to content the 2026-09-25 guide audit found unobtainable on this server.
 * Wholly original; doesn't modify or redistribute the mods it touches.
 */
public class ContentFixes implements ModInitializer {

    @Override
    public void onInitialize() {
        addMissingSpiderSpawns();
        addClutteredSaplingLoot();
        Invokers.register();
        Camels.register();
        AfkFishing.loadConfig();
        AfkImmunity.register();
        Mimics.register();
        LootContainerProtection.register();
        HotbarSlotScore.register();
        NoVillagers.register();
    }

    /**
     * Spider Overhaul 0.0.6 registers 11 spider variants (entity type, attributes, spawn
     * placement, loot, model) but only adds natural spawns for 6. These four are complete
     * mobs with no spawn entry; weights and group sizes match the six that do spawn. The
     * Ocean Spider is left out on purpose: it ships with no entity model or texture.
     * Mushroom Fields are a monster-free biome players rely on, so the Mushroom Spider lives
     * in the Bioshroom Caves (giant underground mushrooms) instead.
     */
    private static void addMissingSpiderSpawns() {
        int weight = SpiderOverhaulConfig.scaleWeight(100);
        BiomeModifications.addSpawn(BiomeSelectors.includeByKey(Biomes.DEEP_DARK),
            MobCategory.MONSTER, ModEntities.SCULK_SPIDER, weight, 1, 3);
        BiomeModifications.addSpawn(BiomeSelectors.tag(BiomeTags.IS_TAIGA),
            MobCategory.MONSTER, ModEntities.TAIGA_SPIDER, weight, 1, 3);
        BiomeModifications.addSpawn(BiomeSelectors.tag(BiomeTags.IS_SAVANNA),
            MobCategory.MONSTER, ModEntities.SAVANNA_SPIDER, weight, 1, 3);
        BiomeModifications.addSpawn(BiomeSelectors.includeByKey(ResourceKey.create(Registries.BIOME,
                Identifier.fromNamespaceAndPath("regions_unexplored", "bioshroom_caves"))),
            MobCategory.MONSTER, ModEntities.MUSHROOM_SPIDER, weight, 1, 3);
    }

    /** Chests that can hold a Cluttered sapling or mushroom, and the chance per chest. */
    private static final Set<String> SAPLING_CHESTS = Set.of(
        "minecraft:chests/shipwreck_supply", "minecraft:chests/ruined_portal");
    private static final double SAPLING_CHANCE = 0.25;
    private static final List<String> SAPLINGS = List.of(
        "cluttered:willow_sapling", "cluttered:poplar_sapling", "cluttered:crabapple_sapling",
        "cluttered:sycamore_sapling", "cluttered:fluorescent_maple_sapling",
        "cluttered:blue_roundhead", "cluttered:fly_agaric");

    /**
     * Cluttered's seven woods only ever came from wandering trader trades (disabled here)
     * and a sapling chest table nothing references, and its trees never generate. One
     * random sapling or mushroom now has a chance to turn up in shipwreck supply chests
     * (cargo) and ruined portal chests (common everywhere).
     */
    private static void addClutteredSaplingLoot() {
        LootTableEvents.MODIFY_DROPS.register((table, context, drops) -> {
            String id = table.unwrapKey().map(k -> k.identifier().toString()).orElse("");
            if (!SAPLING_CHESTS.contains(id) || context.getRandom().nextDouble() >= SAPLING_CHANCE) {
                return;
            }
            String pick = SAPLINGS.get(context.getRandom().nextInt(SAPLINGS.size()));
            Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(pick));
            if (item != null && item != net.minecraft.world.item.Items.AIR) {
                drops.add(new ItemStack(item));
            }
        });
    }
}
