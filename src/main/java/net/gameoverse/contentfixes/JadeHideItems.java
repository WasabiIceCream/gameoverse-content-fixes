package net.gameoverse.contentfixes;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/**
 * Jade skips dropped items: Interactic already shows their tooltip under the crosshair,
 * so both drew the same information. Jade's hide-entities.json would do this too, but
 * it is a client file (never synced by Jade, and client-owned after AutoModpack's first
 * download); a built-in hide ships in the jar, reaches every player, and survives
 * Jade's config reloads.
 */
@WailaPlugin
public class JadeHideItems implements IWailaPlugin {
    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.entityTypeOperations().hide(
                ResourceKey.create(Registries.ENTITY_TYPE, Identifier.withDefaultNamespace("item")));
    }
}
