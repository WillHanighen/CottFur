package xyz.cottageindustries.cottfur.client.model

import net.minecraft.util.Identifier
import software.bernie.geckolib.model.GeoModel
import software.bernie.geckolib.renderer.base.GeoRenderState
import xyz.cottageindustries.cottfur.CottfurConstants

/**
 * GeckoLib GeoModel implementation for anthro player models.
 * This class provides the model, texture, and animation resources to GeckoLib.
 * 
 * In GeckoLib 5.4, GeoModel uses GeoRenderState for resource lookups.
 */
class AnthroGeoModel(
    private val modelType: AnthroModelType
) : GeoModel<AnthroPlayerAnimatable>() {
    
    override fun getModelResource(renderState: GeoRenderState): Identifier {
        return Identifier.of(CottfurConstants.MOD_ID, "geo/${modelType.modelId}.geo")
    }
    
    override fun getTextureResource(renderState: GeoRenderState): Identifier {
        return Identifier.of(CottfurConstants.MOD_ID, "textures/entity/${modelType.modelId}.png")
    }
    
    override fun getAnimationResource(animatable: AnthroPlayerAnimatable): Identifier {
        return Identifier.of(CottfurConstants.MOD_ID, "animations/${modelType.modelId}.animation")
    }
    
    companion object {
        // Cache of models per type
        private val modelCache = mutableMapOf<AnthroModelType, AnthroGeoModel>()
        
        /**
         * Get or create a GeoModel for the specified type
         */
        fun getModel(type: AnthroModelType): AnthroGeoModel {
            return modelCache.getOrPut(type) { AnthroGeoModel(type) }
        }
    }
}
