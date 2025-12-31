package xyz.cottageindustries.cottfur.data

import net.minecraft.util.Identifier
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Data class representing a player's anthro model configuration.
 * 
 * This immutable data class stores all customization options for a player's
 * anthro model, including species type, colors, patterns, and custom textures.
 * Instances are stored in [PlayerModelDataManager] and synchronized over the network.
 * 
 * @property modelTypeId The string ID of the selected model type (e.g., "protogen", "k9", "none")
 * @property customTextureId Optional identifier for a user-imported custom texture
 * @property primaryColor Primary body/fur color as a packed RGB integer (0xRRGGBB)
 * @property secondaryColor Secondary marking color as a packed RGB integer
 * @property accentColor Accent/highlight color as a packed RGB integer
 * @property patternId Optional identifier for the selected fur pattern
 */
data class PlayerModelConfig(
    val modelTypeId: String = "none",
    val customTextureId: String? = null,
    val primaryColor: Int = 0xFFFFFF,
    val secondaryColor: Int = 0x888888,
    val accentColor: Int = 0xFF0000,
    val patternId: String? = null
) {
    companion object {
        val DEFAULT = PlayerModelConfig()
    }
}

/**
 * Server and client-side storage for player model configurations.
 * 
 * This singleton manages all player model configurations in a thread-safe manner.
 * It is accessed from multiple threads:
 * - Netty threads (network packet handling)
 * - Render thread (model rendering)
 * - Main client thread (UI interactions)
 * 
 * Uses [ConcurrentHashMap] internally to ensure thread safety without explicit locking.
 */
object PlayerModelDataManager {
    
    private val playerConfigs = ConcurrentHashMap<UUID, PlayerModelConfig>()
    
    /**
     * Gets the model configuration for a player.
     * 
     * @param playerId The UUID of the player
     * @return The player's config, or [PlayerModelConfig.DEFAULT] if not set
     */
    fun getConfig(playerId: UUID): PlayerModelConfig {
        return playerConfigs.getOrDefault(playerId, PlayerModelConfig.DEFAULT)
    }
    
    /**
     * Sets or updates the model configuration for a player.
     * 
     * @param playerId The UUID of the player
     * @param config The new configuration to store
     */
    fun setConfig(playerId: UUID, config: PlayerModelConfig) {
        playerConfigs[playerId] = config
    }
    
    /**
     * Removes a player's configuration from storage.
     * 
     * Typically called when a player disconnects from the server to free memory.
     * 
     * @param playerId The UUID of the player to remove
     */
    fun removeConfig(playerId: UUID) {
        playerConfigs.remove(playerId)
    }
    
    /**
     * Checks if a player has an anthro model configured (not using default player model).
     * 
     * @param playerId The UUID of the player to check
     * @return `true` if the player has selected an anthro model, `false` if using default or not configured
     */
    fun hasAnthroModel(playerId: UUID): Boolean {
        val config = playerConfigs[playerId] ?: return false
        return config.modelTypeId != "none"
    }
    
    /**
     * Returns a snapshot of all stored player configurations.
     * 
     * Used by the server to sync all existing player models to a newly joined client.
     * Returns a copy to avoid concurrent modification issues.
     * 
     * @return An immutable copy of all player configurations
     */
    fun getAllConfigs(): Map<UUID, PlayerModelConfig> {
        return playerConfigs.toMap()
    }
    
    /**
     * Clears all stored player configurations.
     * 
     * Called on the client when disconnecting from a server to reset state.
     */
    fun clear() {
        playerConfigs.clear()
    }
}

