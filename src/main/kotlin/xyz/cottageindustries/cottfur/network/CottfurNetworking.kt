package xyz.cottageindustries.cottfur.network

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier
import xyz.cottageindustries.cottfur.CottfurConstants
import xyz.cottageindustries.cottfur.data.PlayerModelConfig
import xyz.cottageindustries.cottfur.data.PlayerModelDataManager
import java.util.UUID

/**
 * Main networking handler for CottFur.
 * Handles registration and processing of all mod network packets.
 */
object CottfurNetworking {
    
    // Packet IDs
    val UPDATE_MODEL_ID: CustomPayload.Id<UpdateModelPayload> = 
        CustomPayload.Id(CottfurConstants.id("update_model"))
    val SYNC_ALL_MODELS_ID: CustomPayload.Id<SyncAllModelsPayload> = 
        CustomPayload.Id(CottfurConstants.id("sync_all_models"))
    val SYNC_SINGLE_MODEL_ID: CustomPayload.Id<SyncSingleModelPayload> = 
        CustomPayload.Id(CottfurConstants.id("sync_single_model"))
    
    /**
     * Register all packets on the server side.
     * Call this during server/common mod initialization.
     */
    fun registerServerPackets() {
        CottfurConstants.LOGGER.info("Registering CottFur server packets...")
        
        // Register payload types
        PayloadTypeRegistry.playC2S().register(UPDATE_MODEL_ID, UpdateModelPayload.CODEC)
        PayloadTypeRegistry.playS2C().register(SYNC_ALL_MODELS_ID, SyncAllModelsPayload.CODEC)
        PayloadTypeRegistry.playS2C().register(SYNC_SINGLE_MODEL_ID, SyncSingleModelPayload.CODEC)
        
        // Handle incoming model updates from clients
        ServerPlayNetworking.registerGlobalReceiver(UPDATE_MODEL_ID) { payload, context ->
            val player = context.player()
            
            CottfurConstants.LOGGER.debug("Received model update from player: ${player.name.string}")
            
            // Update the player's model config
            val config = PlayerModelConfig(
                modelTypeId = payload.modelTypeId,
                customTextureId = payload.customTextureId,
                primaryColor = payload.primaryColor,
                secondaryColor = payload.secondaryColor,
                accentColor = payload.accentColor,
                patternId = payload.patternId
            )
            
            PlayerModelDataManager.setConfig(player.uuid, config)
            
            // Broadcast to all other players
            val syncPayload = SyncSingleModelPayload(
                playerUuid = player.uuid.toString(),
                modelTypeId = config.modelTypeId,
                customTextureId = config.customTextureId,
                primaryColor = config.primaryColor,
                secondaryColor = config.secondaryColor,
                accentColor = config.accentColor,
                patternId = config.patternId
            )
            
            // Send to all players on the server
            val server = context.server()
            server.playerManager.playerList.forEach { otherPlayer ->
                if (otherPlayer.uuid != player.uuid) {
                    ServerPlayNetworking.send(otherPlayer, syncPayload)
                }
            }
        }
        
        CottfurConstants.LOGGER.info("CottFur server packets registered!")
    }
}

/**
 * Payload sent from client to server when a player updates their model.
 */
data class UpdateModelPayload(
    val modelTypeId: String,
    val customTextureId: String?,
    val primaryColor: Int,
    val secondaryColor: Int,
    val accentColor: Int,
    val patternId: String?
) : CustomPayload {
    
    override fun getId(): CustomPayload.Id<out CustomPayload> = CottfurNetworking.UPDATE_MODEL_ID
    
    companion object {
        val CODEC: PacketCodec<RegistryByteBuf, UpdateModelPayload> = PacketCodec.tuple(
            PacketCodecs.STRING, UpdateModelPayload::modelTypeId,
            PacketCodecs.optional(PacketCodecs.STRING), { it.customTextureId?.let { s -> java.util.Optional.of(s) } ?: java.util.Optional.empty() },
            PacketCodecs.INTEGER, UpdateModelPayload::primaryColor,
            PacketCodecs.INTEGER, UpdateModelPayload::secondaryColor,
            PacketCodecs.INTEGER, UpdateModelPayload::accentColor,
            PacketCodecs.optional(PacketCodecs.STRING), { it.patternId?.let { s -> java.util.Optional.of(s) } ?: java.util.Optional.empty() }
        ) { modelTypeId, customTextureId, primaryColor, secondaryColor, accentColor, patternId ->
            UpdateModelPayload(
                modelTypeId,
                customTextureId.orElse(null),
                primaryColor,
                secondaryColor,
                accentColor,
                patternId.orElse(null)
            )
        }
    }
}

/**
 * Payload sent from server to client to sync all players' model data.
 * Sent when a player joins the server.
 */
data class SyncAllModelsPayload(
    val playerConfigs: Map<String, PlayerModelConfig>
) : CustomPayload {
    
    override fun getId(): CustomPayload.Id<out CustomPayload> = CottfurNetworking.SYNC_ALL_MODELS_ID
    
    companion object {
        // Simplified codec for the map - we'll serialize it as a count followed by entries
        val CODEC: PacketCodec<RegistryByteBuf, SyncAllModelsPayload> = object : PacketCodec<RegistryByteBuf, SyncAllModelsPayload> {
            override fun decode(buf: RegistryByteBuf): SyncAllModelsPayload {
                val count = buf.readVarInt()
                val configs = mutableMapOf<String, PlayerModelConfig>()
                repeat(count) {
                    val uuid = buf.readString()
                    val modelTypeId = buf.readString()
                    val hasCustomTexture = buf.readBoolean()
                    val customTextureId = if (hasCustomTexture) buf.readString() else null
                    val primaryColor = buf.readInt()
                    val secondaryColor = buf.readInt()
                    val accentColor = buf.readInt()
                    val hasPattern = buf.readBoolean()
                    val patternId = if (hasPattern) buf.readString() else null
                    
                    configs[uuid] = PlayerModelConfig(
                        modelTypeId, customTextureId, primaryColor, secondaryColor, accentColor, patternId
                    )
                }
                return SyncAllModelsPayload(configs)
            }
            
            override fun encode(buf: RegistryByteBuf, value: SyncAllModelsPayload) {
                buf.writeVarInt(value.playerConfigs.size)
                value.playerConfigs.forEach { (uuid, config) ->
                    buf.writeString(uuid)
                    buf.writeString(config.modelTypeId)
                    buf.writeBoolean(config.customTextureId != null)
                    config.customTextureId?.let { buf.writeString(it) }
                    buf.writeInt(config.primaryColor)
                    buf.writeInt(config.secondaryColor)
                    buf.writeInt(config.accentColor)
                    buf.writeBoolean(config.patternId != null)
                    config.patternId?.let { buf.writeString(it) }
                }
            }
        }
    }
}

/**
 * Payload sent from server to client to sync a single player's model data.
 * Sent when any player changes their model.
 */
data class SyncSingleModelPayload(
    val playerUuid: String,
    val modelTypeId: String,
    val customTextureId: String?,
    val primaryColor: Int,
    val secondaryColor: Int,
    val accentColor: Int,
    val patternId: String?
) : CustomPayload {
    
    override fun getId(): CustomPayload.Id<out CustomPayload> = CottfurNetworking.SYNC_SINGLE_MODEL_ID
    
    companion object {
        val CODEC: PacketCodec<RegistryByteBuf, SyncSingleModelPayload> = object : PacketCodec<RegistryByteBuf, SyncSingleModelPayload> {
            override fun decode(buf: RegistryByteBuf): SyncSingleModelPayload {
                val playerUuid = buf.readString()
                val modelTypeId = buf.readString()
                val hasCustomTexture = buf.readBoolean()
                val customTextureId = if (hasCustomTexture) buf.readString() else null
                val primaryColor = buf.readInt()
                val secondaryColor = buf.readInt()
                val accentColor = buf.readInt()
                val hasPattern = buf.readBoolean()
                val patternId = if (hasPattern) buf.readString() else null
                
                return SyncSingleModelPayload(
                    playerUuid, modelTypeId, customTextureId, primaryColor, secondaryColor, accentColor, patternId
                )
            }
            
            override fun encode(buf: RegistryByteBuf, value: SyncSingleModelPayload) {
                buf.writeString(value.playerUuid)
                buf.writeString(value.modelTypeId)
                buf.writeBoolean(value.customTextureId != null)
                value.customTextureId?.let { buf.writeString(it) }
                buf.writeInt(value.primaryColor)
                buf.writeInt(value.secondaryColor)
                buf.writeInt(value.accentColor)
                buf.writeBoolean(value.patternId != null)
                value.patternId?.let { buf.writeString(it) }
            }
        }
    }
}

