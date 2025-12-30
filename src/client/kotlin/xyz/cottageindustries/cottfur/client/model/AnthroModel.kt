package xyz.cottageindustries.cottfur.client.model

import net.minecraft.util.Identifier
import xyz.cottageindustries.cottfur.CottfurConstants

/**
 * Base class for anthro player models.
 * Each anthro type (Protogen, K9, Feline, etc.) will extend this base class.
 * 
 * This is a simplified model definition that provides resource locations.
 * The actual GeckoLib integration happens in the renderer.
 */
abstract class AnthroModel(
    val modelType: AnthroModelType
) {
    /**
     * Get the model resource location
     */
    fun getModelResource(): Identifier {
        return modelType.getModelLocation()
    }
    
    /**
     * Get the texture resource location
     */
    fun getTextureResource(customTexture: Identifier?): Identifier {
        return customTexture ?: modelType.getDefaultTextureLocation()
    }
    
    /**
     * Get the animation resource location
     */
    fun getAnimationResource(): Identifier {
        return modelType.getAnimationLocation()
    }

    companion object {
        const val BONE_HEAD = "head"
        const val BONE_BODY = "body"
        const val BONE_LEFT_ARM = "left_arm"
        const val BONE_RIGHT_ARM = "right_arm"
        const val BONE_LEFT_LEG = "left_leg"
        const val BONE_RIGHT_LEG = "right_leg"
        const val BONE_TAIL = "tail"
        const val BONE_LEFT_EAR = "left_ear"
        const val BONE_RIGHT_EAR = "right_ear"
    }
}

/**
 * Interface for objects that can be animated with the anthro model system.
 */
interface AnthroAnimatable : software.bernie.geckolib.animatable.GeoAnimatable {
    /**
     * Get the custom texture for this animatable, or null to use the default
     */
    fun getCustomTexture(): Identifier?
    
    /**
     * Get the model type for this animatable
     */
    fun getModelType(): AnthroModelType
}
