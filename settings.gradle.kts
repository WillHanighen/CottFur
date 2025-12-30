pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/") {
            name = "Fabric"
        }
        maven("https://maven.fabricmc.net/snapshot/") {
            name = "Fabric Snapshot"
        }
        gradlePluginPortal()
    }
}

rootProject.name = "CottFur"
