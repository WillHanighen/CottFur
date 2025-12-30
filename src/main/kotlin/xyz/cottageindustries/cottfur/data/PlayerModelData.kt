package xyz.cottageindustries.cottfur.data

import net.minecraft.util.Identifier
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Data class representing a player's anthro model configuration.
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
 * This is thread-safe and can be accessed from any thread.
 */
object PlayerModelDataManager {
    
    private val playerConfigs = ConcurrentHashMap<UUID, PlayerModelConfig>()
    
    /**
     * Get the model config for a player, or the default if not set
     */
    fun getConfig(playerId: UUID): PlayerModelConfig {
        return playerConfigs.getOrDefault(playerId, PlayerModelConfig.DEFAULT)
    }
    
    /**
     * Set the model config for a player
     */
    fun setConfig(playerId: UUID, config: PlayerModelConfig) {
        playerConfigs[playerId] = config
    }
    
    /**
     * Remove a player's config (e.g., when they disconnect)
     */
    fun removeConfig(playerId: UUID) {
        playerConfigs.remove(playerId)
    }
    
    /**
     * Check if a player has a non-default model configured
     */
    fun hasAnthroModel(playerId: UUID): Boolean {
        val config = playerConfigs[playerId] ?: return false
        return config.modelTypeId != "none"
    }
    
    /**
     * Get all player configs (for syncing on server join)
     */
    fun getAllConfigs(): Map<UUID, PlayerModelConfig> {
        return playerConfigs.toMap()
    }
    
    /**
     * Clear all configs (e.g., on disconnect)
     */
    fun clear() {
        playerConfigs.clear()
    }
}

