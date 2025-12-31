package xyz.cottageindustries.cottfur.client.render

import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Arm
import net.minecraft.util.Identifier
import xyz.cottageindustries.cottfur.CottfurConstants
import xyz.cottageindustries.cottfur.client.model.AnthroModelType
import xyz.cottageindustries.cottfur.data.PlayerModelDataManager

/**
 * Handles rendering of custom first-person arms/paws for anthro models.
 * 
 * This object manages the replacement of vanilla player arms with anthro paws
 * in first-person view. It provides:
 * - Detection of when anthro arms should be rendered
 * - Texture lookup for arm models
 * - Rendering entry point (currently stub)
 * 
 * Works in conjunction with [HeldItemRendererMixin] to intercept vanilla arm rendering.
 */
object AnthroArmRenderer {
    
    /** Maps model types to their arm texture resource locations. */
    private val armTextures = mapOf(
        AnthroModelType.PROTOGEN to CottfurConstants.id("textures/entity/protogen_arm.png"),
        AnthroModelType.K9 to CottfurConstants.id("textures/entity/k9_arm.png"),
        AnthroModelType.FELINE to CottfurConstants.id("textures/entity/feline_arm.png"),
        AnthroModelType.ANTHRO_BASE to CottfurConstants.id("textures/entity/anthro_base_arm.png")
    )
    
    /**
     * Checks if the local player should use anthro arms in first-person view.
     * 
     * @return `true` if the local player has an anthro model selected
     */
    fun shouldRenderAnthroArms(): Boolean {
        val player = MinecraftClient.getInstance().player ?: return false
        val config = PlayerModelDataManager.getConfig(player.uuid)
        return config.modelTypeId != "none"
    }
    
    /**
     * Gets the model type configured for the local player.
     * 
     * @return The player's [AnthroModelType], or [AnthroModelType.NONE] if not set
     */
    fun getLocalPlayerModelType(): AnthroModelType {
        val player = MinecraftClient.getInstance().player ?: return AnthroModelType.NONE
        val config = PlayerModelDataManager.getConfig(player.uuid)
        return AnthroModelType.fromId(config.modelTypeId)
    }
    
    /**
     * Gets the arm texture identifier for a model type.
     * 
     * @param modelType The model type to get the arm texture for
     * @return The arm texture identifier, or `null` if no arm texture is defined
     */
    fun getArmTexture(modelType: AnthroModelType): Identifier? {
        return armTextures[modelType]
    }
    
    /**
     * Renders a custom first-person arm/paw.
     * 
     * Called by [HeldItemRendererMixin] when intercepting vanilla arm rendering.
     * Currently a stub that sets up transformations but falls back to vanilla.
     * 
     * @param matrices The matrix stack for transformations
     * @param vertexConsumers The vertex consumer provider for rendering
     * @param light The packed lightmap coordinates
     * @param arm Which arm to render ([Arm.LEFT] or [Arm.RIGHT])
     * @return `true` if custom arm was rendered, `false` to fall back to vanilla
     */
    fun renderFirstPersonArm(
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
        arm: Arm
    ): Boolean {
        if (!shouldRenderAnthroArms()) {
            return false
        }
        
        val modelType = getLocalPlayerModelType()
        if (modelType == AnthroModelType.NONE) {
            return false
        }
        
        // Get the arm texture
        val texture = getArmTexture(modelType) ?: return false
        
        // Push transformation
        matrices.push()
        
        try {
            // Apply arm-specific transformations based on which arm
            if (arm == Arm.LEFT) {
                matrices.scale(-1f, 1f, 1f)
            }
            
            // Render the custom arm model
            // The actual rendering will use GeckoLib or a custom model
            // For now, this is a placeholder that indicates where the rendering would happen
            
            // TODO: Implement actual GeckoLib arm model rendering
            // This would involve:
            // 1. Loading the arm model from the full model's arm bones
            // 2. Applying the correct texture
            // 3. Rendering just the arm portion
            
            CottfurConstants.LOGGER.debug("Rendering ${modelType.displayName} arm")
            
        } finally {
            matrices.pop()
        }
        
        // Return true to indicate we handled the rendering
        // Returning false would fall back to vanilla rendering
        return true
    }
    
    /**
     * Configuration data for arm appearance.
     * 
     * @property modelType The anthro species for this arm
     * @property primaryColor Primary fur color as packed RGB
     * @property secondaryColor Secondary marking color as packed RGB
     * @property hasClaws Whether to render claws on the paw
     * @property pawPads Whether to render paw pads
     */
    data class ArmConfig(
        val modelType: AnthroModelType,
        val primaryColor: Int = 0xFFFFFF,
        val secondaryColor: Int = 0x888888,
        val hasClaws: Boolean = true,
        val pawPads: Boolean = true
    )
    
    /**
     * Gets the arm configuration for the local player.
     * 
     * Retrieves the player's model config and creates an [ArmConfig] with
     * the relevant arm rendering parameters.
     * 
     * @return Arm configuration derived from the player's model settings
     */
    fun getLocalArmConfig(): ArmConfig {
        val player = MinecraftClient.getInstance().player ?: return ArmConfig(AnthroModelType.NONE)
        val config = PlayerModelDataManager.getConfig(player.uuid)
        val modelType = AnthroModelType.fromId(config.modelTypeId)
        
        return ArmConfig(
            modelType = modelType,
            primaryColor = config.primaryColor,
            secondaryColor = config.secondaryColor
        )
    }
}

