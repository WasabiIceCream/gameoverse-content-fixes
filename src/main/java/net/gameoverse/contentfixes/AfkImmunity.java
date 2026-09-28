package net.gameoverse.contentfixes;

import com.sakuraryoko.afkplus.impl.config.ConfigWrap;
import com.sakuraryoko.afkplus.impl.player.AfkPlayer;
import com.sakuraryoko.afkplus.impl.player.AfkPlayerList;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Util;

/**
 * AFK damage immunity only after a real idle. AfkPlus grants it the moment a player becomes AFK,
 * including through /afk, so /afk worked as an instant invulnerability toggle. AfkHandlerMixin
 * refuses it until the player's idle time (vanilla's last action time, which AfkPlus resets on
 * movement and looking) reaches AfkPlus's own AFK timeout; this tick check grants it once they get
 * there while still AFK.
 */
public final class AfkImmunity {
    private AfkImmunity() {
    }

    public static boolean idleLongEnough(ServerPlayer player) {
        long timeoutMs = ConfigWrap.pack().timeoutSeconds * 1000L;
        return Util.getMillis() - player.getLastActionTime() >= timeoutMs;
    }

    static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % 20 != 0) return;
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                AfkPlayer afk = AfkPlayerList.getInstance().getPlayer(player);
                if (afk != null && afk.isAfk() && afk.isDamageEnabled() && !afk.isLockDamageEnabled()
                        && idleLongEnough(player)) {
                    afk.getHandler().disableDamage();
                }
            }
        });
    }
}
