package xyz.cottageindustries.cottfur.client

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator

/**
 * Fabric Data Generation entrypoint for CottFur.
 * 
 * Used to generate data files (language files, recipes, etc.) during the build process.
 * Run with `./gradlew runDatagen`.
 * 
 * Currently empty - reserved for future data generation needs.
 */
class CottfurDataGenerator : DataGeneratorEntrypoint {

    /**
     * Called by Fabric to set up data generation providers.
     * 
     * @param fabricDataGenerator The data generator instance to register providers with
     */
    override fun onInitializeDataGenerator(fabricDataGenerator: FabricDataGenerator) {
        val pack = fabricDataGenerator.createPack()
        // TODO: Add data providers here (language, recipes, etc.)
    }
}
