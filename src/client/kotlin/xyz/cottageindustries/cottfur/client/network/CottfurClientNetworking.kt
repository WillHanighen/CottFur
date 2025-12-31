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
 * 
 * This object handles:
 * - Registering receivers for server-to-client sync packets
 * - Sending model update packets to the server
 * - Checking server compatibility
 * 
 * ## Received Packets
 * - [SyncAllModelsPayload]: Full sync on server join
 * - [SyncSingleModelPayload]: Single player update broadcast
 * 
 * ## Sent Packets
 * - [UpdateModelPayload]: Local player model changes
 * 
 * @see CottfurNetworking for packet definitions and server-side handling
 */
object CottfurClientNetworking {
    
    /**
     * Registers client-side packet receivers with Fabric's networking API.
     * 
     * Must be called during client mod initialization ([CottfurClient.onInitializeClient]).
     * Payload types are already registered on the common side in [CottfurNetworking].
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
     * Sends the local player's model configuration to the server.
     * 
     * Called when the player applies changes in the customization screen.
     * If the server doesn't have CottFur installed (checked via [isServerSupported]),
     * the packet is not sent and a warning is logged.
     * 
     * @param config The new model configuration to send
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
     * Checks if the connected server supports CottFur networking.
     * 
     * Uses Fabric's networking API to check if the server can receive our packets.
     * Returns `false` if in singleplayer or on a vanilla/non-CottFur server.
     * 
     * @return `true` if the server has CottFur installed and can receive model updates
     */
    fun isServerSupported(): Boolean {
        return ClientPlayNetworking.canSend(CottfurNetworking.UPDATE_MODEL_ID)
    }
}

