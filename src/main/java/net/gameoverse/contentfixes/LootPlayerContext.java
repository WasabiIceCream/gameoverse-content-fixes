package net.gameoverse.contentfixes;

import net.minecraft.world.entity.player.Player;

/**
 * The player opening a Go Fish crate, while its loot rolls. Go Fish builds the crate's loot
 * context with only a position, so loot modifiers that need a player (Apotheosis gems and affix
 * conversion) find none. See ApotheosisLootPlayerMixin.
 */
public final class LootPlayerContext {
    public static final ThreadLocal<Player> CRATE_OPENER = new ThreadLocal<>();

    private LootPlayerContext() {
    }
}
