plugins {
    id("fabric-loom") version "1.17.20"
    `java-library`
}

version = project.property("mod_version") as String
group = project.property("maven_group") as String

base {
    archivesName.set(project.property("archives_base_name") as String)
}

dependencies {
    minecraft("com.mojang:minecraft:${project.property("minecraft_version")}")
    implementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")
    implementation("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_version")}")
    // Spider Overhaul, compile-only against the exact jar the server runs.
    compileOnly(files("../../fabric 26.1/mods/SpiderOverhaul-0.0.6-Fabric-v26.1.jar"))
    // Creatures and Beasts, for its anvil logic (CnbAnvilMixin).
    compileOnly(files("../../fabric 26.1/mods/CreaturesAndBeasts-Fabric-1.0.4+26.1.x.jar"))
    // Field Guide, for its client loot cache (FieldGuideCacheMixin).
    compileOnly(files("../../fabric 26.1/mods/fieldguide-fabric-26.1.2-1.7.11.jar"))
    // Dynamic Difficulty, for its level nameplate check (DifficultyLevelPlateMixin).
    compileOnly(files("../../fabric 26.1/mods/dynamic_difficulty-fabric-1.3.3+26.1.2.jar"))
    // Bits and Balance, for its quick-harvest drops (QuickHarvestDropsMixin).
    compileOnly(files("../../fabric 26.1/mods/bitsandbalance-fabric-26.1-2.4.0.jar"))
    // Apotheosis and Go Fish, for gems/affixes from fishing and crates (ApotheosisLootPlayerMixin, GoFishCrateMixin).
    compileOnly(files("../../fabric 26.1/mods/apotheosis-adventure-fabric-0.2.0.jar"))
    compileOnly(files("../../fabric 26.1/mods/go-fish-1.11.0+26.1.jar"))
    // Jade, for hiding dropped items (JadeHideItems).
    compileOnly(files("../../fabric 26.1/mods/Jade-mc26.1-Fabric-26.1.11.jar"))
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("fabric.mod.json") {
        expand(mapOf("version" to project.version))
    }
}
