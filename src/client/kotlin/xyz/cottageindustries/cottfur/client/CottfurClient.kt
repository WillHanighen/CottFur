package xyz.cottageindustries.cottfur.client

import net.fabricmc.api.ClientModInitializer
import xyz.cottageindustries.cottfur.CottfurConstants
import xyz.cottageindustries.cottfur.client.model.ModelRegistry
import xyz.cottageindustries.cottfur.client.network.CottfurClientNetworking

class CottfurClient : ClientModInitializer {

    override fun onInitializeClient() {
        CottfurConstants.LOGGER.info("Initializing CottFur client...")
        
        // Initialize the model registry
        ModelRegistry.initialize()
        
        // Register client-side networking
        CottfurClientNetworking.registerClientPackets()
        
        // Register keybinds
        CottfurKeybinds.register()
        
        CottfurConstants.LOGGER.info("CottFur client initialization complete!")
    }
}
