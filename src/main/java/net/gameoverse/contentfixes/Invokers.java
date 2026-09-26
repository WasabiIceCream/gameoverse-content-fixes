package net.gameoverse.contentfixes;

import java.util.Set;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings;

/**
 * Illager Invasion's Invoker only ever spawned in raids, and raids can't happen here: a
 * village center needs a villager-claimed bed or job site, and this server has no villagers
 * (spawn-npcs=false). The Invoker is the only source of Primal Essence, which the Imbuing
 * Table needs. Three sources instead:
 * <ul>
 *   <li>a rare spawn among the pillagers inside any structure whose own spawn list has
 *       pillagers (every outpost variant, the illager fort), in {@code NaturalSpawnerMixin};</li>
 *   <li>a rare Dark Forest spawn (monster category, so night or dark);</li>
 *   <li>Primal Essence in outpost treasure chests and mansion chests.</li>
 * </ul>
 */
public final class Invokers {
    public static final Identifier INVOKER = Identifier.fromNamespaceAndPath("illagerinvasion", "invoker");
    private static final Identifier PRIMAL_ESSENCE = Identifier.fromNamespaceAndPath("illagerinvasion", "primal_essence");

    /** Existing structure spawn weights are multiplied by this and the Invoker gets weight 1. */
    public static final int STRUCTURE_WEIGHT_SCALE = 40;
    private static final int DARK_FOREST_WEIGHT = 1;

    private static final Set<String> ESSENCE_CHESTS = Set.of(
        "minecraft:chests/pillager_outpost", "minecraft:chests/woodland_mansion",
        "nova_structures:chests/pillager_outpost_treasure", "nova_structures:chests/pillager_outpost_small_treasure",
        "nova_structures:chests/mansion_overhaul/mansion_overhaul_library",
        "nova_structures:chests/mansion_overhaul/mansion_overhaul_generic");
    private static final double ESSENCE_CHANCE = 0.15;

    private Invokers() {
    }

    public static EntityType<?> invokerType() {
        return BuiltInRegistries.ENTITY_TYPE.getOptional(INVOKER).orElse(null);
    }

    static void register() {
        BiomeModifications.create(Identifier.fromNamespaceAndPath("gameoverse_content_fixes", "dark_forest_invoker"))
            .add(ModificationPhase.ADDITIONS, BiomeSelectors.includeByKey(Biomes.DARK_FOREST), context -> {
                EntityType<?> invoker = invokerType();
                if (invoker != null) {
                    context.getMobSpawnSettings().addSpawn(MobCategory.MONSTER,
                        new MobSpawnSettings.SpawnerData(invoker, 1, 1), DARK_FOREST_WEIGHT);
                }
            });

        LootTableEvents.MODIFY_DROPS.register((table, context, drops) -> {
            String id = table.unwrapKey().map(k -> k.identifier().toString()).orElse("");
            if (!ESSENCE_CHESTS.contains(id) || context.getRandom().nextDouble() >= ESSENCE_CHANCE) {
                return;
            }
            Item essence = BuiltInRegistries.ITEM.getValue(PRIMAL_ESSENCE);
            if (essence != null && essence != Items.AIR) {
                drops.add(new ItemStack(essence));
            }
        });
    }
}
