package xyz.cottageindustries.cottfur.client.model

import net.minecraft.util.Identifier
import xyz.cottageindustries.cottfur.CottfurConstants

/**
 * Enum representing the different anthro model types (species) available in the mod.
 * 
 * Each type has:
 * - A unique string identifier for network serialization and storage
 * - A human-readable display name for the UI
 * - Associated resource locations for models, textures, and animations
 * 
 * Use [NONE] to represent the default vanilla player model.
 * 
 * @property displayName Human-readable name shown in the customization UI
 * @property modelId Unique string identifier used in configs and networking
 */
enum class AnthroModelType(
    val displayName: String,
    val modelId: String
) {
    NONE("None (Default Player)", "none"),
    PROTOGEN("Protogen", "protogen"),
    K9("Canine (K9)", "k9"),
    FELINE("Feline", "feline"),
    ANTHRO_BASE("Basic Anthro", "anthro_base");

    /**
     * Gets the GeckoLib geometry file location for this model type.
     * 
     * @return Identifier pointing to `assets/cottfur/geo/{modelId}.geo.json`
     */
    fun getModelLocation(): Identifier {
        return Identifier.of(CottfurConstants.MOD_ID, "geo/$modelId.geo.json")
    }

    /**
     * Gets the default texture location for this model type.
     * 
     * Custom textures may override this via [PlayerModelConfig.customTextureId].
     * 
     * @return Identifier pointing to `assets/cottfur/textures/entity/{modelId}.png`
     */
    fun getDefaultTextureLocation(): Identifier {
        return Identifier.of(CottfurConstants.MOD_ID, "textures/entity/$modelId.png")
    }

    /**
     * Gets the GeckoLib animation file location for this model type.
     * 
     * @return Identifier pointing to `assets/cottfur/animations/{modelId}.animation.json`
     */
    fun getAnimationLocation(): Identifier {
        return Identifier.of(CottfurConstants.MOD_ID, "animations/$modelId.animation.json")
    }

    /**
     * Checks if this type represents an actual anthro model.
     * 
     * @return `true` for all types except [NONE], which represents the vanilla player model
     */
    fun isAnthroModel(): Boolean = this != NONE

    companion object {
        /**
         * Looks up a model type by its string identifier.
         * 
         * @param id The model ID string (e.g., "protogen", "k9")
         * @return The matching [AnthroModelType], or [NONE] if not found
         */
        fun fromId(id: String): AnthroModelType {
            return entries.find { it.modelId == id } ?: NONE
        }

        /**
         * Returns all model types that represent actual anthro models.
         * 
         * Excludes [NONE], which represents the default vanilla player.
         * Useful for populating selection UIs.
         * 
         * @return List of all anthro model types
         */
        fun anthroTypes(): List<AnthroModelType> {
            return entries.filter { it.isAnthroModel() }
        }
    }
}

