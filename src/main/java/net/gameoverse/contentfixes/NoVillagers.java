package net.gameoverse.contentfixes;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The server has no villagers at all (villages don't generate, {@code spawn-npcs=false},
 * Wandering Traders off), and that includes zombie villagers, which a player could cure. Two gaps
 * were left: every biome's monster list carries the zombie villager (the vanilla 5% zombie
 * variant), and many structures place villagers or zombie villagers directly (mineshaft prison
 * rooms, captives, camps, ships, Moog's Mineshafts' workers; 20+ mods, found by scanning every
 * jar's structure files). Chasing each structure would never end, so:
 * <ul>
 *   <li>zombie villagers come out of every biome's spawn list, so those spawns go to other mobs;</li>
 *   <li>any villager or zombie villager is refused when it would enter a world, whether spawned,
 *       placed by a structure, converted, or loaded with an old chunk.</li>
 * </ul>
 * Only those two exact entity types: modded villager-like mobs and Wandering Traders (a gamerule)
 * are untouched.
 */
final class NoVillagers {
    private static final Logger LOG = LoggerFactory.getLogger("gameoverse_content_fixes");

    private NoVillagers() {
    }

    static void register() {
        BiomeModifications.create(Identifier.fromNamespaceAndPath("gameoverse_content_fixes", "no_zombie_villagers"))
            .add(ModificationPhase.REMOVALS, BiomeSelectors.all(),
                context -> context.getMobSpawnSettings().removeSpawnsOfEntityType(EntityType.ZOMBIE_VILLAGER));

        ServerEntityEvents.ALLOW_LOAD.register((entity, level, reason, loadedFromDisk) -> {
            EntityType<?> type = entity.getType();
            if (type != EntityType.VILLAGER && type != EntityType.ZOMBIE_VILLAGER) {
                return true;
            }
            LOG.info("Removed a {} at {} {} ({})", EntityType.getKey(type), level.dimension().identifier(),
                entity.blockPosition().toShortString(), loadedFromDisk ? "loaded" : reason != null ? reason : "added");
            return false;
        });
    }
}
