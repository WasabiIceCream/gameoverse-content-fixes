package net.gameoverse.contentfixes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;

/**
 * AFK fishing is slower than fishing by hand. While AfkPlus marks the rod's owner as AFK (4 minutes
 * without moving), the hook only makes fishing progress every Nth tick, so the wait for a bite,
 * the fish's approach and the nibble all take N times as long and AFK fishing yields about 1/N as
 * much of everything. Moving clears AFK, so active fishers keep full speed. N is
 * {@code afkFishingSlowdown} in config/gameoverse_content_fixes.json (1 turns it off).
 */
public final class AfkFishing {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int DEFAULT_SLOWDOWN = 2;
    private static int slowdown = DEFAULT_SLOWDOWN;

    private AfkFishing() {
    }

    public static int slowdown() {
        return slowdown;
    }

    static void loadConfig() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve("gameoverse_content_fixes.json");
        try {
            JsonObject json = Files.exists(path)
                ? GSON.fromJson(Files.readString(path), JsonObject.class)
                : new JsonObject();
            if (json == null) json = new JsonObject();
            if (!json.has("afkFishingSlowdown")) {
                json.addProperty("afkFishingSlowdown", DEFAULT_SLOWDOWN);
                Files.writeString(path, GSON.toJson(json));
            }
            slowdown = Math.max(1, json.get("afkFishingSlowdown").getAsInt());
        } catch (IOException | RuntimeException e) {
            slowdown = DEFAULT_SLOWDOWN;
        }
    }
}
