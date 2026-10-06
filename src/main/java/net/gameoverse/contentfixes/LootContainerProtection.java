package net.gameoverse.contentfixes;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * SlashLoot gives every player their own loot from a world-generated container by keeping its
 * loot table on the container instead of letting vanilla roll it once. Destroying the container
 * would end that for everyone after the first visitor, like breaking a dungeon's spawner, so a
 * container that still has a loot table can't be broken by survival players, blown up, or (for
 * chest minecarts and chest boats) damaged at all. That is SlashLoot's own test for which
 * containers it handles, so this covers structure chests, dungeon chests (a world-gen feature,
 * not a structure) and modded loot containers alike, and never a container a player placed.
 * Creative mode and damage that bypasses invulnerability ({@code /kill}, the void) still work.
 */
public final class LootContainerProtection {
    private static final Component MESSAGE =
        Component.translatableWithFallback("message.gameoverse_content_fixes.loot_container_protected",
            "Everyone gets their own loot from this container, so it can't be broken.");

    private LootContainerProtection() {
    }

    static void register() {
        PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, blockEntity) -> {
            if (player.isCreative() || !isLootContainer(blockEntity)) {
                return true;
            }
            player.sendOverlayMessage(MESSAGE);
            return false;
        });
    }

    /** Used by the explosion mixin: these block positions are left out of the blast. */
    public static boolean isLootContainer(BlockEntity blockEntity) {
        return blockEntity instanceof RandomizableContainer container && container.getLootTable() != null;
    }

    /** Used by the vehicle mixin: true if this damage must not reach a loot minecart or boat. */
    public static boolean protectsVehicle(Entity vehicle, DamageSource source) {
        if (!(vehicle instanceof ContainerEntity container) || container.getContainerLootTable() == null) {
            return false;
        }
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return false;
        }
        if (source.getEntity() instanceof Player player) {
            if (player.isCreative()) {
                return false;
            }
            player.sendOverlayMessage(MESSAGE);
        }
        return true;
    }
}
