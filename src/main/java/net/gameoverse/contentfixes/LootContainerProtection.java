package net.gameoverse.contentfixes;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.RandomizableContainer;

/**
 * SlashLoot gives every player their own loot from a world-generated container by keeping its
 * loot table on the block instead of letting vanilla roll it once. Breaking the container
 * would end that for everyone after the first visitor, like breaking a dungeon's spawner, so
 * survival players can't break a container that still has a loot table. That is SlashLoot's
 * own test for which containers it handles, so this covers structure chests, dungeon chests
 * (a world-gen feature, not a structure) and modded loot containers alike, and never a chest
 * a player placed. Creative mode can still break them.
 */
final class LootContainerProtection {
    private LootContainerProtection() {
    }

    static void register() {
        PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, blockEntity) -> {
            if (player.isCreative()) {
                return true;
            }
            if (blockEntity instanceof RandomizableContainer container && container.getLootTable() != null) {
                player.sendOverlayMessage(Component.literal(
                    "Everyone gets their own loot from this container, so it can't be broken."));
                return false;
            }
            return true;
        });
    }
}
