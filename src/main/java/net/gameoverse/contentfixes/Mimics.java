package net.gameoverse.contentfixes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.UUID;

import artifacts.entity.MimicEntity;
import artifacts.registry.ModEntityTypes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

/**
 * Artifacts' Mimic only generated at its own campsites, so a campsite told you to expect one. Now any single loot
 * chest a structure generates has a {@code mimicChestChance} (config/gameoverse_content_fixes.json) to be a dormant
 * Mimic instead, holding that chest's loot table: killed, it drops the chest's loot as well as its own artifact.
 * The tell: a dormant Mimic sits 20-35 degrees off square (see {@link #skew}), which a placed chest never does.
 */
public final class Mimics {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final double DEFAULT_CHANCE = 0.05;
    private static double chance = DEFAULT_CHANCE;

    /** The loot table of the chest a Mimic replaced. */
    public static final AttachmentType<ResourceKey<LootTable>> CHEST_LOOT = AttachmentRegistry.create(
        Identifier.fromNamespaceAndPath("gameoverse_content_fixes", "mimic_chest_loot"),
        builder -> builder.persistent(ResourceKey.codec(Registries.LOOT_TABLE)));

    private Mimics() {
    }

    static void register() {
        loadConfig();
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            ResourceKey<LootTable> key = entity.getAttached(CHEST_LOOT);
            if (key == null || !(entity.level() instanceof ServerLevel level)) {
                return;
            }
            Player player = source.getEntity() instanceof Player p ? p : null;
            LootParams.Builder params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, entity.position())
                .withOptionalParameter(LootContextParams.THIS_ENTITY, player);
            if (player != null) {
                params.withLuck(player.getLuck());
            }
            LootTable table = level.getServer().reloadableRegistries().getLootTable(key);
            table.getRandomItems(params.create(LootContextParamSets.CHEST)).forEach(stack -> entity.spawnAtLocation(level, stack));
        });
    }

    /**
     * Called after a chunk's structures and features are placed. The roll is seeded by world seed and position, so it's
     * the same for a given chest whatever order chunks generate in.
     */
    public static void replaceChests(WorldGenLevel level, ChunkAccess chunk) {
        if (chance <= 0) {
            return;
        }
        for (BlockPos pos : new ArrayList<>(chunk.getBlockEntitiesPos())) {
            BlockState state = chunk.getBlockState(pos);
            if (!state.is(Blocks.CHEST) || state.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
                continue;
            }
            ResourceKey<LootTable> loot = lootTable(chunk, pos);
            // Campsites already roll their own Mimics.
            if (loot == null || loot.identifier().getNamespace().equals("artifacts")) {
                continue;
            }
            // Buried and walled-in chests: a Mimic there would suffocate.
            BlockPos above = pos.above();
            if (!level.getBlockState(above).getCollisionShape(level, above).isEmpty()) {
                continue;
            }
            RandomSource random = RandomSource.create(level.getSeed() ^ pos.asLong() * 0x9E3779B97F4A7C15L);
            if (random.nextDouble() >= chance) {
                continue;
            }
            // STRUCTURE, not CHUNK_GENERATION: see CampsiteMimicReasonMixin.
            MimicEntity mimic = ModEntityTypes.MIMIC.get().create(level.getLevel(), EntitySpawnReason.STRUCTURE);
            if (mimic == null) {
                continue;
            }
            level.setBlock(pos, state.getValue(ChestBlock.WATERLOGGED) ? Blocks.WATER.defaultBlockState()
                : Blocks.AIR.defaultBlockState(), 2);
            mimic.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            mimic.setDormant(true);
            mimic.setFacing(state.getValue(ChestBlock.FACING));
            mimic.setAttached(CHEST_LOOT, loot);
            level.addFreshEntity(mimic);
        }
    }

    private static ResourceKey<LootTable> lootTable(ChunkAccess chunk, BlockPos pos) {
        BlockEntity be = chunk.getBlockEntity(pos);
        if (be instanceof RandomizableContainerBlockEntity container) {
            return container.getLootTable();
        }
        CompoundTag pending = chunk.getBlockEntityNbt(pos);
        if (pending == null) {
            return null;
        }
        return pending.getString("LootTable")
            .map(id -> ResourceKey.create(Registries.LOOT_TABLE, Identifier.parse(id)))
            .orElse(null);
    }

    /** Turns a dormant Mimic 20-35 degrees off its facing, left or right, fixed per Mimic (from its UUID). */
    public static void skew(MimicEntity mimic) {
        Direction facing = mimic.facing;
        if (!mimic.isDormant || facing == null) {
            return;
        }
        UUID id = mimic.getUUID();
        long bits = id.getLeastSignificantBits() ^ id.getMostSignificantBits();
        float offset = (20 + Math.floorMod(bits, 16)) * ((bits >>> 8 & 1) == 0 ? 1 : -1);
        float yaw = facing.toYRot() + offset;
        if (mimic.getMoveControl() instanceof MimicDirection control) {
            control.gameoverse$setDirection(yaw);
        }
        mimic.setYRot(yaw);
        mimic.setYBodyRot(yaw);
        mimic.setYHeadRot(yaw);
    }

    private static void loadConfig() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve("gameoverse_content_fixes.json");
        try {
            JsonObject json = Files.exists(path)
                ? GSON.fromJson(Files.readString(path), JsonObject.class)
                : new JsonObject();
            if (json == null) json = new JsonObject();
            if (!json.has("mimicChestChance")) {
                json.addProperty("mimicChestChance", DEFAULT_CHANCE);
                Files.writeString(path, GSON.toJson(json));
            }
            chance = json.get("mimicChestChance").getAsDouble();
        } catch (IOException | RuntimeException e) {
            chance = DEFAULT_CHANCE;
        }
    }
}
