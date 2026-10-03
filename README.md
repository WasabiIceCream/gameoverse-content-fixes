# Gameoverse Content Fixes

Small Fabric mod (26.1.2) that gives a source to content the 2026-09-25 guide
audit found unobtainable on this server. The parts that only need config or a
datapack live in `../gameoverse-obtainability-fixes/`. Wholly original, MIT.

## Spider spawns

Spider Overhaul 0.0.6 registers 11 spider variants (entity, attributes, spawn
placement, loot, model) but only adds natural spawns for 6. This adds the other
four through Fabric API's `BiomeModifications.addSpawn`, using the same weight
(`SpiderOverhaulConfig.scaleWeight(100)`) and group size (1-3) as the six that do
spawn:

| Spider | Biomes |
|---|---|
| Sculk Spider | Deep Dark |
| Taiga Spider | `#minecraft:is_taiga` |
| Savanna Spider | `#minecraft:is_savanna` |
| Mushroom Spider | Regions Unexplored Bioshroom Caves (Mushroom Fields stay monster-free) |

The Ocean Spider is left out on purpose: it's unfinished upstream (Spider Overhaul's
plans list the "Spider Crab" as still to add). Summoned, it's invisible and dies within
seconds. Its drop, the Crab Leg, stays unobtainable.

## Cluttered saplings

Cluttered's woods only came from wandering trader trades (disabled here) and a
chest table nothing references, and its trees never generate. A
`LootTableEvents.MODIFY_DROPS` hook gives shipwreck supply chests and ruined portal
chests a 25% chance to add one random Willow, Poplar, Crabapple, Sycamore or
Fluorescent Maple sapling, Blue Roundhead or Fly Agaric.

## Signal Rail item model

Minecart Chain 1.0.8 ships the Signal Rail's block states and item model but not the
`assets/minecart_chain/items/signal_rail.json` item definition that 1.21.4+ needs, so
the item showed as a magenta/black cube (placed rails were fine, they reuse the
powered rail's block models). This mod's jar supplies that one file; it's harmless if
Minecart Chain isn't installed.

## Build and deploy

`./gradlew build` builds against Spider Overhaul straight from
`../../fabric 26.1/mods/` (`compileOnly`). Copy
`build/libs/gameoverse-content-fixes-<version>.jar` to `fabric 26.1/mods/`. It's
`environment: "*"`, so it's also distributed to clients through AutoModpack's
normal server mod sync.

## Creatures and Beasts anvil recipes and Yeti Hide armor (1.0.2, 1.0.3)

Creatures and Beasts 1.0.4's Fabric port has two dead hooks:

- Its anvil mixin injects at the third `ItemStack.isEmpty()` in `AnvilMenu.createResult`.
  On 26.1.2 that call sits in the rename section, after vanilla has already returned for
  anything that isn't a repair material or a matching damageable item, so Yeti Hide onto
  armor and the Heal Spell Book tier-up never produced a result. Found by exporting the
  merged class (`-Dmixin.debug.export=true`). `CnbAnvilMixin` calls the mod's own
  `CNBEvents.onAnvilChange` at `HEAD` and uses its cost, count and result.
- `CNBEvents.onItemAttributeModifierCalculate` (the Yeti Hide armor bonus, read from the
  `HideAmount` counter) is never called on Fabric. `CnbYetiHideAttributeMixin` calls it from
  both `ItemStack.forEachModifier` overloads (live stats and tooltips), the same pattern as
  `gameoverse-material-traits`.

Tested in game: tier 2 Heal Spell Book, hides apply and show the extra armor line. The
anvil charges nothing for hides on unenchanted armor; that's Simple Smithing Overhaul's
`freeUnenchantedRepairs = true`, not this fix. Compiles against the installed Creatures and
Beasts jar (`compileOnly`); retest when it updates.

## Invokers (1.0.4)

Illager Invasion's Invoker only spawned in raids, and raids can't happen here: vanilla's
village check (`PoiManager.isVillageCenter`) only counts villager-claimed (`IS_OCCUPIED`)
beds and job sites, and this server has no villagers (`spawn-npcs=false`). Found by trying
to start one with placed beds and Bad Omen; `/locate poi minecraft:home` saw the beds but
Bad Omen never turned into Raid Omen. The Invoker is the only source of Primal Essence (the
Imbuing Table). Three sources, all in `Invokers`:

- `NaturalSpawnerInvokerMixin`: in any structure whose own monster spawn list has pillagers
  (vanilla and Dungeons & Taverns outposts, all 32 Towns and Towers outposts, the illager
  fort, the illusioner training grounds, Explorations' underground temple), existing
  weights are multiplied by 40 and the Invoker is added at weight 1 (about 1 in 41 spawns).
- Dark Forest monster spawn, weight 1, added at server start through a biome modification
  (the entity type isn't registered yet when this mod initialises).
- 15% chance of one Primal Essence in outpost treasure and mansion chests (vanilla and
  Dungeons & Taverns tables). Loot rolls confirmed it drops; the spawns need an in-game look.

## Camels (1.0.5)

No Villages removes desert villages, and their camel pens were the only reliable source of
camels. What's left is the natural spawn at weight 1 (rabbits are 12). This raises it to
3 in the vanilla Desert and in Regions Unexplored's Baobab Savanna, Dry Bushland and Steppe,
and adds camels at 3 to Regions Unexplored's Saguaro and Joshua Deserts, which had none
(`BiomeModifications`, post-processing phase: remove the camel entry, add it back). Camels
only spawn on `#minecraft:camels_spawnable_on` (sand), so the three grassy biomes rarely
get one. Checked on fresh terrain: one camel in 256 new chunks around both a Saguaro Desert
and a vanilla Desert.

## Field Guide loot cache (1.0.6)

Field Guide 1.7.11 sends every entry's loot to the client on join, 100 entries per packet, and
the client re-reads its whole cache file (`fieldguide_cache/<session>/drops.nbt`, 4.2 MB here),
merges and rewrites it on the render thread for each packet: ~9 s of every join (JFR and file
timestamps, 2026-09-27). The client keeps the loot in memory, and asks the server for anything
missing, so `FieldGuideCacheMixin` (client-only) skips both the write and the read. Join went
from ~20 s to ~15 s locally. Retest or drop when Field Guide updates.

## Dynamic Difficulty level plates (1.0.7)

Dynamic Difficulty 1.3.3 draws a level nameplate (and Jade line) for every mob not in the client's
`hiddenLevelEntities` list, so mobs with no level (cows, and since difficulty-hearts 1.5.2 fish,
squid, bats, villagers, traders, allays) showed "Level 1". The server only syncs levels for mobs
that have one (plus players), so `DifficultyLevelPlateMixin` (client-only) hides the plate when
the client has no level for a non-player mob. Confirmed in game 2026-09-27.

## Jade hides dropped items (1.0.8)

Interactic already draws a dropped item's tooltip under the crosshair, so Jade showed the same
information a second time. `JadeHideItems` (a `jade` entrypoint) adds `minecraft:item` to Jade's
built-in hide list. Jade's own `hide-entities.json` can't do this for players: Jade never sends it
from the server, and AutoModpack stops updating client configs after the first download. The
built-in list ships in this jar and is copied back in on every Jade config reload.

## Quick-harvest drops go through the block (1.0.9)

Bits and Balance 2.4.0's quick harvest (right-click a mature crop, bare hand or hoe) builds its
drops by running the block's loot table directly, skipping any `Block#getDrops` override.
Mystical Agriculture's crops have no loot table and create essence, a second seed and Fertilized
Essence in that override, so a quick harvest dropped nothing and still replanted.
`QuickHarvestDropsMixin` replaces Bits and Balance's `collectDrops` with vanilla
`Block.getDrops(state, level, pos, blockEntity, player, tool)`, which passes the same loot
parameters through the block; loot-table crops drop as before. Re-check the method name and
signature when Bits and Balance updates (the mixin is required, so a mismatch fails loudly).

## Quick harvest: off-hand fallthrough and the break event (1.0.10)

Two more Bits and Balance quick-harvest fixes in `QuickHarvestDropsMixin`:
- Holding right-click with bone meal or MA fertilizer, the game retries the use; once the crop
  matured the main-hand item no longer applied, the use fell through to an empty off-hand, and
  Bits and Balance (which treats an empty hand as a harvest) harvested it on the same press. Now
  the off-hand only quick-harvests when the main hand is empty too.
- It destroys the crop through the level, never as a player break, so nothing listening to
  Fabric's `PlayerBlockBreakEvents.AFTER` saw it: Skill Tree and Skill Proficiencies XP, Heart
  Crystal and Rumor crop drops. It now fires `AFTER` after a successful harvest.

Also ships `assets/gameoverse_content_fixes/lang/en_us.json`: readable text for the
`gameoverse-farming-path` World Tier criteria ("Or: grow Tertium Essence" and so on) on
Apotheosis's tier screen, which lists each criterion by its lang key. Confirmed in game.

## Apotheosis gems and affixes from fishing and crates (1.0.11)

Apotheosis only adds gems (`gem_loot_injection`) and converts gear to affixed gear
(`affix_conversion`) when `GenContext.findPlayer` finds a player in the loot context. Vanilla
fishing passes the bobber as the loot entity, and Go Fish crates build their context with only a
position, so neither ever got gems or affixes. `ApotheosisLootPlayerMixin` falls back to the
bobber's owner, then to the player opening a crate, which `GoFishCrateMixin` records while the
crate's loot rolls (synchronously inside `CrateItem.use`). Confirmed in game: a gem and an affixed
chestplate from 8 Golden Crates. The fishing-bobber path is the same code but not yet seen in game.

## AFK rules and AFK fishing (1.0.12)

Built with the user so AFK fishing (XPlus Autofish, a client-only mod we ship) is safe but slower
than fishing by hand. Works with AfkPlus, whose idle clock is vanilla's last action time.
- `RodActivityMixin`: casting/reeling a rod and its arm swing no longer reset idle time (vanilla
  `handleUseItem`/`handleAnimate`), and neither do chat messages or commands (`tryHandleChat`, the
  path for chat and both command packets). Moving and, with AfkPlus `resetOnLook: true`, looking
  still reset it. Without this an auto-fisher was never marked AFK.
- `FishingHookAfkMixin` + `AfkFishing`: while the rod's owner is AFK, `FishingHook.catchingFish`
  only runs every Nth tick, so bites take N times as long and AFK fishing yields about 1/N of
  everything. N = `afkFishingSlowdown` in `config/gameoverse_content_fixes.json` (default 2, 1 = off).
- `AfkHandlerMixin` + `AfkImmunity`: AfkPlus granted damage immunity the moment a player became AFK,
  so `/afk` was an instant invulnerability toggle. Immunity is now refused until the player's real
  idle time reaches AfkPlus's AFK timeout (`timeoutSeconds`, 240 s), and granted by a once-a-second
  check when they get there while still AFK.
Server config that goes with it: AfkPlus `resetOnLook: true`, `afkCommandCooldown: 1` (was 5, which
swallowed a quick second `/afk`), and LuckPerms `afkplus.kick.safe` on `vouched`. All confirmed in game.

## core-lib-api language file (1.0.13)

AfkPlus bundles Core Lib API 0.3.0, which loads `/assets/core-lib-api/lang/en_us.json` through
its classloader but ships the file at `assets/corelib/lang/`, so every boot logged two ERRORs
(`i18nLang#load: Error; file not found`). Fabric's classloader searches every mod jar, so this
jar carries the file at the path it asks for (same one string). Upstream fixed the path in
`sakura-ryoko/corelib` commit `4d32b511` (2026-09-22) after the 0.3.0 release; drop the file once
AfkPlus bundles a newer Core Lib API. Field Guide stays on 1.7.11: 1.19.0 clears the main depth
buffer after the level renders (`AfterLevelOverlay`), which breaks Eclipse (sky over terrain).

## No glowing outlines in Iris's shadow pass (1.0.14)

Iris's shadow pass extracts entity render states itself, glowing outlines included, and the outline
vertices written there are never drawn or flushed: they pile up frame after frame until
`BufferBuilder` throws "Trying to write too many vertices (>16777215)". Better Item Despawn makes
every dropped item within 48 blocks glow, so standing near about 30 drops with a shader crashed the
client in under a minute (found 2026-09-28 testing dragon loot). `ShadowPassOutlineMixin` makes
`shouldEntityAppearGlowing` report false while `IrisApi.isRenderingShadowPass()` is true; the normal
pass still draws the glow. Iris is only touched when it's loaded (`IrisShadowPass`).

## Hidden curses keep their descriptions hidden (1.0.18)

Sneaky Curses hides an unrevealed curse in its tooltip handler (`ItemTooltipHandler.onItemTooltip`)
by reading each line's own contents: a translatable `enchantment.<ns>.<path>` key becomes rune
text, a longer key (`enchantment.<ns>.<path>.desc`) is removed. Dynamic Tooltips and Item Tooltips
build their description lines as `Component.literal("").append(...)`, so the key sits in a child
and a hidden curse's description still showed. `SneakyCurseDescriptionMixin` wraps that
`getContents()` call: when a line's own contents aren't an `enchantment.` key, it hands Sneaky
Curses the first child (up to three levels deep) whose key starts with `enchantment.` and has more
than three dot parts, so the wrapped description is removed like a plain one. Name lines are
unaffected (only description keys are substituted). Client mixin; Sneaky Curses is now a
dependency. Found 2026-09-30.

## One effect description per tooltip (1.0.19)

Effect Insights (through its bundled Tooltip Insights library, `TooltipDescriptionsHandler.modifyTooltip`) adds an
effect's description after every tooltip line that names the effect. An Apotheosis Potion Charm names its effect four
times (title, "Applies...", "Source:", potion contents), so its tooltip repeated the same description four times.
`TooltipInsightsOnceMixin` (client, `@Pseudo`, `require = 0`) wraps the handler's key map so `containsKey` answers yes
only for the last line naming each effect; that line gets the description (and Effect Insights' value formatting).
Check this still applies when Effect Insights updates.

## Mimics in any structure chest (1.0.20)

Artifacts 15.1.3's Mimic only generated at its underground campsites, so a campsite told you to expect one. `Mimics`
(hooked at the end of `ChunkGenerator.applyBiomeDecoration`, `ChunkGeneratorMimicMixin`) turns a single
`minecraft:chest` with a loot table into a dormant Mimic facing the same way, at `mimicChestChance` in
`config/gameoverse_content_fixes.json` (default 0.05). Skipped: double chests, Artifacts' own tables (campsites roll
their own), and chests with anything solid above (buried treasure). The roll is seeded by world seed and position. The
Mimic keeps the chest's loot table in a persistent Fabric attachment and drops that loot on death, rolled with the
killer as the chest opener. Since 1.0.21 such a Mimic drops no artifact of its own (`LootTableEvents.MODIFY_DROPS`
clears `artifacts:entities/mimic` when the Mimic carries a chest table): the chest's loot already has Artifacts' usual
chest chance (`artifactRarity`), so artifacts stay as rare as before. Campsite Mimics keep the guaranteed artifact.

The tell (`MimicSkewMixin`, `MimicControllerMixin`): every dormant Mimic, campsite ones included, sits 20-35 degrees
off its facing, fixed per Mimic from its UUID. A placed chest is always square.

`CampsiteMimicReasonMixin`: Respawning Animals cancels every mob that joins with the `CHUNK_GENERATION` spawn reason
(its "world_gen" animals, `AnimalSpawningHandler.onEntityJoin`), and logs "Mismatched spawn type" while moving the
entity type to the CREATURE category. Artifacts spawns campsite Mimics with that reason, so campsites never had a
Mimic on this server. Both campsite Mimics and ours now use `STRUCTURE` (its "scripted" reason, not touched).

Tested locally at chance 1.0 (2026-09-30): 15 Mimics in 7x7 fresh chunks, all off square, no "Mismatched spawn"
warning; killing a trial chamber supplies Mimic dropped an artifact plus the supplies loot. Re-check `placeChest`
and the Mimic's `setFacing`/`readAdditionalSaveData` when Artifacts updates (the mixins are required).

## Bucketry remainders (1.0.22)

`BucketryBucketRemainderMixin` and `BucketryMilkRemainderMixin` give Bucketry's (`buckets_update`) filled buckets a
per-stack `FabricItem.getCraftingRemainder(ItemStack)`. Its water and lava buckets leave a fresh empty bucket through
`craftRemainder` (a worn wooden or bamboo bucket came back repaired), and its milk buckets had no remainder at all, so a
cake ate a gold milk bucket. Both now leave the matching empty bucket with the filled one's `DAMAGE` kept, as Bucketry
itself does when the bucket is emptied by hand (`toEmpty`/`copyState`). Pairs with `gameoverse-bucket-variants`, which
lets recipes accept these buckets. A recipe type that reads `Item.getCraftingRemainder()` without the stack still gets
Bucketry's own remainder. Re-check `BaseBucketItem.isEmpty/toEmpty` and `BaseMilkBucketItem.emptyCounterpart/copyState`
when Bucketry updates (the mixins are required).

## Loot containers can't be destroyed (1.0.23-1.0.24)

SlashLoot gives each player their own loot by leaving the loot table on a world-generated container (its own test for
which containers it handles: a `RandomizableContainer` with a loot table). Destroying one ended that for everyone,
like breaking a dungeon spawner. `LootContainerProtection`: survival players can't break such a block
(`PlayerBlockBreakEvents.BEFORE`, overlay message); `ExplosionLootContainerMixin` drops those positions from
`ServerExplosion.interactWithBlocks`' list (not `calculateExplodedPositions`' return: Lithium replaces that method from
its head, so a RETURN hook never ran); `VehicleLootContainerMixin` cancels `VehicleEntity.hurtServer` for chest
minecarts and chest boats (`ContainerEntity`) with a loot table. Creative players and damage that bypasses
invulnerability (`/kill`, the void) still destroy them. Player-placed containers never have a loot table. Covers
dungeon chests too (a world-gen feature, not a structure, so an Unbreakables structure rule would miss them). Tested
over RCON with TNT: loot chest, barrel, minecart and boat survive, plain ones are destroyed. Re-check the mixin
targets when Lithium or SlashLoot update.

## Hotbar slot score for the WASD library (1.0.25)

`HotbarSlotScore` sets the `w.hotbar_slot` score of every player to their selected hotbar slot at the end of each
server tick (only when it changes, and only while that objective exists). WASD's library (in `gameoverse-moar-loot`)
read it every tick with `data get entity @s SelectedItemSlot`, a full player NBT serialization costing about 1.1 ms per
player per tick here; the Gameoverse build of that data pack drops the read. Deploy the two together.

## XPlus Autofish settings are the server's (1.0.26)

XPlus Autofish (client-only) lets each player tune it: recast delays, multiple rods, and Auto Turn View, which turns
the camera after each catch. AfkPlus counts looking around as activity (`resetOnLook`), so with Auto Turn View on an
auto-fisher never went AFK and kept full fishing speed (see "AFK fishing" above). Its config,
`config/autofish-client.toml`, is now force-synced by AutoModpack (`!/config/autofish-client.toml` in
`allowEditsInFiles`; source of truth `client-config/autofish-client.toml`, Autofish 2.0.0's defaults), and two client
mixins close the in-game ways to change it: the settings key (V) is never registered, so it isn't in Controls, and
Mod Menu's Configure button gets back the screen it came from. Recheck both targets (`FabricModAutofish.onInitializeClient`'s
`KeyMappingHelper.registerKeyMapping` call, `AutofishScreenBuilder.buildScreen`) when Autofish updates.
