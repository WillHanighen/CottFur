package xyz.cottageindustries.cottfur.client

import net.fabricmc.api.ClientModInitializer
import xyz.cottageindustries.cottfur.CottfurConstants
import xyz.cottageindustries.cottfur.client.debugShit.DebugShitInit
import xyz.cottageindustries.cottfur.client.model.ModelRegistry
import xyz.cottageindustries.cottfur.client.network.CottfurClientNetworking

/**
 * Client-side entrypoint for the CottFur mod.
 * 
 * This class is only loaded on the client and handles initialization of:
 * - Model registry for all anthro species
 * - Client-side networking for receiving model sync packets
 * - Keybind registration for the customization screen
 * 
 * @see ModelRegistry
 * @see CottfurClientNetworking
 * @see CottfurKeybinds
 */
class CottfurClient : ClientModInitializer {

    /**
     * Called by Fabric when the client mod is initialized.
     * 
     * Initialization order is important:
     * 1. Model registry (must be ready before networking)
     * 2. Client networking (to receive model data)
     * 3. Keybinds (UI access)
     */
    override fun onInitializeClient() {
        CottfurConstants.LOGGER.info("Initializing CottFur client...")
        
        // Initialize the model registry
        ModelRegistry.initialize()
        
        // Register client-side networking
        CottfurClientNetworking.registerClientPackets()
        
        // Register keybinds
        CottfurKeybinds.register()

        // Register debug shit. DELETE ME!!!!
        DebugShitInit().debugShitInit()

        CottfurConstants.LOGGER.info("CottFur client initialization complete!")
    }
}
