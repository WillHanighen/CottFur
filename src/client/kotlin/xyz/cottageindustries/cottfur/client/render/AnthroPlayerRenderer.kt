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
 * Player-specific renderer wrapper for anthro models.
 * 
 * This class manages the rendering setup for a single player with an anthro model.
 * It handles:
 * - Setting up the [AnthroPlayerAnimatable] with player-specific data
 * - Populating the [AnthroRenderState] from player entity state
 * - Coordinating with the model registry
 * 
 * Instances are cached per [AnthroModelType] via [getRenderer].
 * 
 * @property modelType The anthro species this renderer handles
 * @property playerAnimatable The GeckoLib animatable for animation state
 */
class AnthroPlayerRenderer(
    private val modelType: AnthroModelType,
    private val playerAnimatable: AnthroPlayerAnimatable
) {
    
    /** The model definition for this type, from the registry. */
    private val model: AnthroModel? = ModelRegistry.getModel(modelType)
    
    /** Render state object for passing data to the GeckoLib renderer. */
    private val renderState = AnthroRenderState()
    
    /**
     * Sets up render state for a player entity.
     * 
     * Populates the [AnthroRenderState] and [AnthroPlayerAnimatable] with data
     * from the player entity, preparing for rendering.
     * 
     * @param player The player entity being rendered
     * @param matrices The matrix stack for transformations
     * @param vertexConsumers The vertex consumer provider
     * @param light The packed lightmap coordinates
     * @param partialTicks The partial tick time for interpolation
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
        /** Cache of renderer instances per model type. */
        private val rendererCache = mutableMapOf<AnthroModelType, AnthroPlayerRenderer>()
        
        /**
         * Gets or creates a cached renderer for the specified model type.
         * 
         * @param modelType The model type to get a renderer for
         * @return The cached renderer, or `null` if [modelType] is [AnthroModelType.NONE]
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
         * Checks if a player entity should be rendered with an anthro model.
         * 
         * @param player The player entity to check
         * @return `true` if the player has selected a non-default anthro model
         */
        fun shouldRenderAnthro(player: AbstractClientPlayerEntity): Boolean {
            val config = PlayerModelDataManager.getConfig(player.uuid)
            return config.modelTypeId != "none"
        }
        
        /**
         * Gets the configured model type for a player.
         * 
         * @param player The player entity to look up
         * @return The player's selected [AnthroModelType], or [AnthroModelType.NONE] if not configured
         */
        fun getPlayerModelType(player: AbstractClientPlayerEntity): AnthroModelType {
            val config = PlayerModelDataManager.getConfig(player.uuid)
            return AnthroModelType.fromId(config.modelTypeId)
        }
    }
}
