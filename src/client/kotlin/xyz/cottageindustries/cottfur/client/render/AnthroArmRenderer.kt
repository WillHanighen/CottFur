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
 * This replaces the default player arms when an anthro model is active.
 */
object AnthroArmRenderer {
    
    // Arm model textures for each model type
    private val armTextures = mapOf(
        AnthroModelType.PROTOGEN to CottfurConstants.id("textures/entity/protogen_arm.png"),
        AnthroModelType.K9 to CottfurConstants.id("textures/entity/k9_arm.png"),
        AnthroModelType.FELINE to CottfurConstants.id("textures/entity/feline_arm.png"),
        AnthroModelType.ANTHRO_BASE to CottfurConstants.id("textures/entity/anthro_base_arm.png")
    )
    
    /**
     * Check if the local player should use anthro arms in first-person.
     */
    fun shouldRenderAnthroArms(): Boolean {
        val player = MinecraftClient.getInstance().player ?: return false
        val config = PlayerModelDataManager.getConfig(player.uuid)
        return config.modelTypeId != "none"
    }
    
    /**
     * Get the model type for the local player's arms.
     */
    fun getLocalPlayerModelType(): AnthroModelType {
        val player = MinecraftClient.getInstance().player ?: return AnthroModelType.NONE
        val config = PlayerModelDataManager.getConfig(player.uuid)
        return AnthroModelType.fromId(config.modelTypeId)
    }
    
    /**
     * Get the texture identifier for the specified arm and model type.
     */
    fun getArmTexture(modelType: AnthroModelType): Identifier? {
        return armTextures[modelType]
    }
    
    /**
     * Render a custom first-person arm.
     * This is called by a mixin that intercepts the vanilla arm rendering.
     * 
     * @param matrices The matrix stack for transformations
     * @param vertexConsumers The vertex consumer provider
     * @param light The packed light value
     * @param arm Which arm to render (main hand or off hand)
     * @return true if custom arm was rendered, false to fall back to vanilla
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
     * Configuration for arm appearance.
     */
    data class ArmConfig(
        val modelType: AnthroModelType,
        val primaryColor: Int = 0xFFFFFF,
        val secondaryColor: Int = 0x888888,
        val hasClaws: Boolean = true,
        val pawPads: Boolean = true
    )
    
    /**
     * Get arm configuration for the local player.
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

