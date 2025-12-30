package xyz.cottageindustries.cottfur.client.model

import net.minecraft.util.Identifier
import xyz.cottageindustries.cottfur.CottfurConstants

/**
 * Enum representing the different anthro model types available in the mod.
 * Each type has a unique identifier and associated model/texture resources.
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
     * Get the model resource location for this type
     */
    fun getModelLocation(): Identifier {
        return Identifier.of(CottfurConstants.MOD_ID, "geo/$modelId.geo.json")
    }

    /**
     * Get the default texture resource location for this type
     */
    fun getDefaultTextureLocation(): Identifier {
        return Identifier.of(CottfurConstants.MOD_ID, "textures/entity/$modelId.png")
    }

    /**
     * Get the animation resource location for this type
     */
    fun getAnimationLocation(): Identifier {
        return Identifier.of(CottfurConstants.MOD_ID, "animations/$modelId.animation.json")
    }

    /**
     * Check if this type represents an actual anthro model (not the default player)
     */
    fun isAnthroModel(): Boolean = this != NONE

    companion object {
        /**
         * Get a model type by its string ID
         */
        fun fromId(id: String): AnthroModelType {
            return entries.find { it.modelId == id } ?: NONE
        }

        /**
         * Get all anthro model types (excluding NONE)
         */
        fun anthroTypes(): List<AnthroModelType> {
            return entries.filter { it.isAnthroModel() }
        }
    }
}

