package xyz.cottageindustries.cottfur.client.render

import net.minecraft.client.network.AbstractClientPlayerEntity
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import xyz.cottageindustries.cottfur.CottfurConstants
import xyz.cottageindustries.cottfur.client.model.AnthroModel
import xyz.cottageindustries.cottfur.client.model.AnthroModelType
import xyz.cottageindustries.cottfur.client.model.AnthroPlayerAnimatable
import xyz.cottageindustries.cottfur.client.model.AnthroRenderState
import xyz.cottageindustries.cottfur.client.model.ModelRegistry
import xyz.cottageindustries.cottfur.data.PlayerModelDataManager

/**
 * Renderer for anthro player models.
 * This renderer is used when a player has selected an anthro model.
 */
class AnthroPlayerRenderer(
    private val modelType: AnthroModelType,
    private val playerAnimatable: AnthroPlayerAnimatable
) {
    
    private val model: AnthroModel? = ModelRegistry.getModel(modelType)
    private val renderState = AnthroRenderState()
    
    /**
     * Render the anthro model for a player
     * 
     * @param player The player entity being rendered
     * @param matrices The matrix stack for transformations
     * @param vertexConsumers The vertex consumer provider
     * @param light The packed light value
     * @param partialTicks The partial tick time
     */
    fun render(
        player: AbstractClientPlayerEntity,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
        partialTicks: Float
    ) {
        model?.let { anthroModel ->
            // Set up the animatable with the player's data
            playerAnimatable.setModelType(modelType)
            
            // Get the player's custom texture if any
            val config = PlayerModelDataManager.getConfig(player.uuid)
            if (config.customTextureId != null) {
                playerAnimatable.setCustomTexture(
                    Identifier.of(CottfurConstants.MOD_ID, "textures/custom/${config.customTextureId}.png")
                )
                renderState.customTexture = playerAnimatable.getCustomTexture()
            } else {
                playerAnimatable.setCustomTexture(null)
                renderState.customTexture = null
            }
            
            // Update render state with player data
            renderState.modelType = modelType
            renderState.headPitch = player.pitch
            renderState.headYaw = player.headYaw
            renderState.bodyYaw = player.bodyYaw
            renderState.isSprinting = player.isSprinting
            renderState.isSneaking = player.isSneaking
            renderState.isSwimming = player.isSwimming
            
            // The actual rendering will be handled by the GeoRenderer
            // For now, we just set up the state
        }
    }
    
    companion object {
        // Cache of renderers per model type
        private val rendererCache = mutableMapOf<AnthroModelType, AnthroPlayerRenderer>()
        
        /**
         * Get or create a renderer for the specified model type
         */
        fun getRenderer(modelType: AnthroModelType): AnthroPlayerRenderer? {
            if (modelType == AnthroModelType.NONE) return null
            
            return rendererCache.getOrPut(modelType) {
                AnthroPlayerRenderer(
                    modelType,
                    AnthroPlayerAnimatable(modelType)
                )
            }
        }
        
        /**
         * Check if a player should be rendered with an anthro model
         */
        fun shouldRenderAnthro(player: AbstractClientPlayerEntity): Boolean {
            val config = PlayerModelDataManager.getConfig(player.uuid)
            return config.modelTypeId != "none"
        }
        
        /**
         * Get the model type for a player
         */
        fun getPlayerModelType(player: AbstractClientPlayerEntity): AnthroModelType {
            val config = PlayerModelDataManager.getConfig(player.uuid)
            return AnthroModelType.fromId(config.modelTypeId)
        }
    }
}
