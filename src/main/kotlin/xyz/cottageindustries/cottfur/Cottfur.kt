package xyz.cottageindustries.cottfur

import net.fabricmc.api.ModInitializer
import xyz.cottageindustries.cottfur.network.CottfurNetworking

/**
 * Main server/common entrypoint for the CottFur mod.
 * 
 * This class is loaded on both client and dedicated server environments.
 * It handles registration of server-side networking packets for model synchronization.
 * 
 * @see CottfurNetworking for packet definitions
 */
class Cottfur : ModInitializer {

    /**
     * Called by Fabric when the mod is initialized.
     * 
     * Registers server-side networking handlers for player model synchronization.
     * This runs before the world loads and is called exactly once per game session.
     */
    override fun onInitialize() {
        CottfurConstants.LOGGER.info("Initializing ${CottfurConstants.MOD_NAME}...")
        
        // Register server-side networking
        CottfurNetworking.registerServerPackets()
        
        CottfurConstants.LOGGER.info("${CottfurConstants.MOD_NAME} initialization complete!")
    }
}
