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

## Build and deploy

`./gradlew build` builds against Spider Overhaul straight from
`../../fabric 26.1/mods/` (`compileOnly`). Copy
`build/libs/gameoverse-content-fixes-<version>.jar` to `fabric 26.1/mods/`. It's
`environment: "*"`, so it's also distributed to clients through AutoModpack's
normal server mod sync.
