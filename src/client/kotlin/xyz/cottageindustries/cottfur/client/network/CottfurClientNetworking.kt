package xyz.cottageindustries.cottfur.client.network

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import xyz.cottageindustries.cottfur.CottfurConstants
import xyz.cottageindustries.cottfur.data.PlayerModelConfig
import xyz.cottageindustries.cottfur.data.PlayerModelDataManager
import xyz.cottageindustries.cottfur.network.CottfurNetworking
import xyz.cottageindustries.cottfur.network.SyncAllModelsPayload
import xyz.cottageindustries.cottfur.network.SyncSingleModelPayload
import xyz.cottageindustries.cottfur.network.UpdateModelPayload
import java.util.UUID

/**
 * Client-side networking handler for CottFur.
 * Handles receiving model sync packets and sending model updates.
 */
object CottfurClientNetworking {
    
    /**
     * Register client-side packet handlers.
     * Call this during client mod initialization.
     * Note: Payload types are registered in CottfurNetworking on the common side.
     */
    fun registerClientPackets() {
        CottfurConstants.LOGGER.info("Registering CottFur client packets...")
        
        // Note: Payload types are already registered in CottfurNetworking.registerServerPackets()
        // We only need to register the client-side receivers here.
        
        // Handle sync of all models (on join)
        ClientPlayNetworking.registerGlobalReceiver(CottfurNetworking.SYNC_ALL_MODELS_ID) { payload, context ->
            CottfurConstants.LOGGER.debug("Received sync all models packet with ${payload.playerConfigs.size} configs")
            
            context.client().execute {
                // Clear existing configs and load the synced ones
                PlayerModelDataManager.clear()
                payload.playerConfigs.forEach { (uuidStr, config) ->
                    try {
                        val uuid = UUID.fromString(uuidStr)
                        PlayerModelDataManager.setConfig(uuid, config)
                    } catch (e: IllegalArgumentException) {
                        CottfurConstants.LOGGER.warn("Invalid UUID in sync packet: $uuidStr")
                    }
                }
            }
        }
        
        // Handle sync of a single model (when another player updates)
        ClientPlayNetworking.registerGlobalReceiver(CottfurNetworking.SYNC_SINGLE_MODEL_ID) { payload, context ->
            CottfurConstants.LOGGER.debug("Received sync single model packet for player: ${payload.playerUuid}")
            
            context.client().execute {
                try {
                    val uuid = UUID.fromString(payload.playerUuid)
                    val config = PlayerModelConfig(
                        modelTypeId = payload.modelTypeId,
                        customTextureId = payload.customTextureId,
                        primaryColor = payload.primaryColor,
                        secondaryColor = payload.secondaryColor,
                        accentColor = payload.accentColor,
                        patternId = payload.patternId
                    )
                    PlayerModelDataManager.setConfig(uuid, config)
                } catch (e: IllegalArgumentException) {
                    CottfurConstants.LOGGER.warn("Invalid UUID in sync packet: ${payload.playerUuid}")
                }
            }
        }
        
        CottfurConstants.LOGGER.info("CottFur client packets registered!")
    }
    
    /**
     * Send a model update to the server.
     * Call this when the local player changes their model configuration.
     */
    fun sendModelUpdate(config: PlayerModelConfig) {
        if (!ClientPlayNetworking.canSend(CottfurNetworking.UPDATE_MODEL_ID)) {
            CottfurConstants.LOGGER.warn("Cannot send model update - server may not have CottFur installed")
            return
        }
        
        val payload = UpdateModelPayload(
            modelTypeId = config.modelTypeId,
            customTextureId = config.customTextureId,
            primaryColor = config.primaryColor,
            secondaryColor = config.secondaryColor,
            accentColor = config.accentColor,
            patternId = config.patternId
        )
        
        ClientPlayNetworking.send(payload)
        CottfurConstants.LOGGER.debug("Sent model update: ${config.modelTypeId}")
    }
    
    /**
     * Check if the server supports CottFur networking.
     */
    fun isServerSupported(): Boolean {
        return ClientPlayNetworking.canSend(CottfurNetworking.UPDATE_MODEL_ID)
    }
}

