package net.gameoverse.contentfixes;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.Scoreboard;

/**
 * WASD Build Team's library (in gameoverse-moar-loot) read every player's selected hotbar slot every tick with
 * {@code data get entity @s SelectedItemSlot}, which serializes the whole player: about 1.1 ms per player per tick
 * here (/perf). The Gameoverse build of that data pack drops the read; this writes the same score from Java instead,
 * only when it changes. Does nothing while the objective doesn't exist (the data pack isn't installed).
 */
final class HotbarSlotScore {
    private static final String OBJECTIVE = "w.hotbar_slot";

    private HotbarSlotScore() {
    }

    static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            Scoreboard scoreboard = server.getScoreboard();
            Objective objective = scoreboard.getObjective(OBJECTIVE);
            if (objective == null) return;
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                int slot = player.getInventory().getSelectedSlot();
                ReadOnlyScoreInfo current = scoreboard.getPlayerScoreInfo(player, objective);
                if (current == null || current.value() != slot) {
                    scoreboard.getOrCreatePlayerScore(player, objective).set(slot);
                }
            }
        });
    }
}
