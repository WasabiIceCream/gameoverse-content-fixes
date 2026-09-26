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

The Ocean Spider is left out on purpose: it ships with no entity model or texture.
Its drop, the Crab Leg, stays unobtainable.

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
