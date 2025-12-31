package xyz.cottageindustries.cottfur.client.model

import net.minecraft.util.Identifier
import xyz.cottageindustries.cottfur.CottfurConstants

/**
 * Abstract base class for anthro player model definitions.
 * 
 * Each anthro species (Protogen, K9, Feline, etc.) extends this class to define
 * species-specific properties. The base class provides:
 * - Resource location helpers for models, textures, and animations
 * - Standard bone name constants for the skeleton hierarchy
 * 
 * Subclasses can add species-specific bone constants (e.g., visor for Protogen).
 * 
 * @property modelType The anthro species this model represents
 */
abstract class AnthroModel(
    val modelType: AnthroModelType
) {
    /**
     * Gets the GeckoLib geometry resource location.
     * 
     * @return Identifier for the `.geo.json` file
     */
    fun getModelResource(): Identifier {
        return modelType.getModelLocation()
    }
    
    /**
     * Gets the texture resource location, with optional custom texture override.
     * 
     * @param customTexture Custom texture identifier, or `null` for default
     * @return The custom texture if provided, otherwise the model's default texture
     */
    fun getTextureResource(customTexture: Identifier?): Identifier {
        return customTexture ?: modelType.getDefaultTextureLocation()
    }
    
    /**
     * Gets the GeckoLib animation resource location.
     * 
     * @return Identifier for the `.animation.json` file
     */
    fun getAnimationResource(): Identifier {
        return modelType.getAnimationLocation()
    }

    companion object {
        // ========== Standard Bone Names ==========
        // These must match the bone names in the .geo.json files
        
        /** Head bone - controls head rotation for look-at. */
        const val BONE_HEAD = "head"
        
        /** Body/torso bone - the main body segment. */
        const val BONE_BODY = "body"
        
        /** Left arm bone - upper arm segment. */
        const val BONE_LEFT_ARM = "left_arm"
        
        /** Right arm bone - upper arm segment. */
        const val BONE_RIGHT_ARM = "right_arm"
        
        /** Left leg bone - upper leg segment. */
        const val BONE_LEFT_LEG = "left_leg"
        
        /** Right leg bone - upper leg segment. */
        const val BONE_RIGHT_LEG = "right_leg"
        
        /** Tail bone - for tail animations. */
        const val BONE_TAIL = "tail"
        
        /** Left ear bone - for ear animations. */
        const val BONE_LEFT_EAR = "left_ear"
        
        /** Right ear bone - for ear animations. */
        const val BONE_RIGHT_EAR = "right_ear"
    }
}

/**
 * Interface for objects that can be animated with the anthro model system.
 * 
 * Extends GeckoLib's [GeoAnimatable] to add CottFur-specific properties
 * for texture and model type retrieval.
 */
interface AnthroAnimatable : software.bernie.geckolib.animatable.GeoAnimatable {
    /**
     * Gets the custom texture for this animatable.
     * 
     * @return Custom texture identifier, or `null` to use the model's default texture
     */
    fun getCustomTexture(): Identifier?
    
    /**
     * Gets the model type (species) for this animatable.
     * 
     * @return The [AnthroModelType] determining which model to render
     */
    fun getModelType(): AnthroModelType
}
