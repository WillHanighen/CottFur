package xyz.cottageindustries.cottfur

import net.fabricmc.api.ModInitializer
import xyz.cottageindustries.cottfur.network.CottfurNetworking

class Cottfur : ModInitializer {

    override fun onInitialize() {
        CottfurConstants.LOGGER.info("Initializing ${CottfurConstants.MOD_NAME}...")
        
        // Register server-side networking
        CottfurNetworking.registerServerPackets()
        
        CottfurConstants.LOGGER.info("${CottfurConstants.MOD_NAME} initialization complete!")
    }
}
