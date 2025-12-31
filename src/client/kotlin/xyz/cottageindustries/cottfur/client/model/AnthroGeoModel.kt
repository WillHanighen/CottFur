package xyz.cottageindustries.cottfur.client.model

import net.minecraft.util.Identifier
import software.bernie.geckolib.model.GeoModel
import software.bernie.geckolib.renderer.base.GeoRenderState
import xyz.cottageindustries.cottfur.CottfurConstants

/**
 * GeckoLib [GeoModel] implementation for anthro player models.
 * 
 * This class provides resource locations for model geometry, textures, and animations
 * to GeckoLib's rendering system. One instance is created per [AnthroModelType] and
 * cached in the companion object.
 * 
 * ## GeckoLib 5.4 API Note
 * In GeckoLib 5.x, resource lookup methods receive [GeoRenderState] instead of entity
 * references. This is a breaking change from GeckoLib 4.x.
 * 
 * @property modelType The anthro species this model represents
 */
class AnthroGeoModel(
    private val modelType: AnthroModelType
) : GeoModel<AnthroPlayerAnimatable>() {
    
    /**
     * Gets the geometry resource location for this model.
     * 
     * @param renderState The current render state (unused, but required by API)
     * @return Identifier pointing to the `.geo.json` file
     */
    override fun getModelResource(renderState: GeoRenderState): Identifier {
        return Identifier.of(CottfurConstants.MOD_ID, "geo/${modelType.modelId}.geo")
    }
    
    /**
     * Gets the texture resource location for this model.
     * 
     * @param renderState The current render state (unused, but required by API)
     * @return Identifier pointing to the default texture PNG
     */
    override fun getTextureResource(renderState: GeoRenderState): Identifier {
        return Identifier.of(CottfurConstants.MOD_ID, "textures/entity/${modelType.modelId}.png")
    }
    
    /**
     * Gets the animation resource location for this model.
     * 
     * @param animatable The animatable instance (can be used to select animations)
     * @return Identifier pointing to the `.animation.json` file
     */
    override fun getAnimationResource(animatable: AnthroPlayerAnimatable): Identifier {
        return Identifier.of(CottfurConstants.MOD_ID, "animations/${modelType.modelId}.animation")
    }
    
    companion object {
        /** Cache of GeoModel instances, one per model type. */
        private val modelCache = mutableMapOf<AnthroModelType, AnthroGeoModel>()
        
        /**
         * Gets or creates a cached [AnthroGeoModel] for the specified type.
         * 
         * Models are cached to avoid creating duplicate instances.
         * 
         * @param type The model type to get
         * @return The cached or newly created GeoModel instance
         */
        fun getModel(type: AnthroModelType): AnthroGeoModel {
            return modelCache.getOrPut(type) { AnthroGeoModel(type) }
        }
    }
}
