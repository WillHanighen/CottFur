package xyz.cottageindustries.cottfur.client.render

import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Arm
import net.minecraft.util.Identifier
import xyz.cottageindustries.cottfur.CottfurConstants
import xyz.cottageindustries.cottfur.client.model.AnthroModel
import xyz.cottageindustries.cottfur.client.model.AnthroModelType

/**
 * Handles rendering of custom anthro arms/paws in first-person view.
 * This replaces the vanilla first-person arm rendering when an anthro model is active.
 */
object FirstPersonArmRenderer {
    
    /**
     * Information about a first-person arm to render.
     */
    data class ArmRenderInfo(
        val modelType: AnthroModelType,
        val customTexture: Identifier?,
        val arm: Arm,
        val swingProgress: Float,
        val equippedProgress: Float
    )
    
    /**
     * Check if we should render a custom first-person arm.
     */
    fun shouldRenderCustomArm(): Boolean {
        val client = MinecraftClient.getInstance()
        val player = client.player ?: return false
        
        // Check if player has an anthro model active
        // TODO: Replace with actual player data lookup when data sync is implemented
        return true // For now, always render custom arms for testing
    }
    
    /**
     * Get the arm texture location for the given model type.
     */
    fun getArmTexture(modelType: AnthroModelType, customTexture: Identifier?): Identifier {
        return customTexture ?: modelType.getDefaultTextureLocation()
    }
    
    /**
     * Get the bone name for the arm based on handedness.
     */
    fun getArmBoneName(arm: Arm): String {
        return when (arm) {
            Arm.LEFT -> AnthroModel.BONE_LEFT_ARM
            Arm.RIGHT -> AnthroModel.BONE_RIGHT_ARM
        }
    }
    
    /**
     * Calculate the arm transformation for first-person view.
     * This mimics the vanilla first-person arm positioning but adapted for anthro paws.
     */
    fun applyFirstPersonArmTransform(
        matrices: MatrixStack,
        arm: Arm,
        swingProgress: Float,
        equippedProgress: Float
    ) {
        val side = if (arm == Arm.RIGHT) 1.0f else -1.0f
        
        // Base position - move arm into view
        matrices.translate(side * 0.56f, -0.52f + equippedProgress * -0.6f, -0.72f)
        
        // Apply swing animation
        if (swingProgress > 0.0f) {
            val swingAmount = kotlin.math.sin(kotlin.math.sqrt(swingProgress) * Math.PI.toFloat())
            val swingAmount2 = kotlin.math.sin(swingProgress * Math.PI.toFloat())
            
            matrices.multiply(
                net.minecraft.util.math.RotationAxis.POSITIVE_Y.rotationDegrees(
                    side * (45.0f + swingAmount * -20.0f)
                )
            )
            matrices.multiply(
                net.minecraft.util.math.RotationAxis.POSITIVE_X.rotationDegrees(
                    swingAmount2 * -80.0f
                )
            )
            matrices.multiply(
                net.minecraft.util.math.RotationAxis.POSITIVE_Y.rotationDegrees(
                    side * -45.0f
                )
            )
        }
        
        // Scale for first-person view
        matrices.scale(0.75f, 0.75f, 0.75f)
    }
    
    /**
     * Apply paw-specific transformations for a more stylized look.
     * Anthro paws may need different positioning than human arms.
     */
    fun applyPawTransform(matrices: MatrixStack, modelType: AnthroModelType, arm: Arm) {
        val side = if (arm == Arm.RIGHT) 1.0f else -1.0f
        
        when (modelType) {
            AnthroModelType.PROTOGEN -> {
                // Protogen have more robotic/angular arms
                matrices.translate(side * 0.02f, 0.05f, 0.0f)
            }
            AnthroModelType.K9 -> {
                // Canine paws - slightly wider spread
                matrices.translate(side * 0.03f, 0.0f, 0.02f)
            }
            AnthroModelType.FELINE -> {
                // Feline paws - more graceful, slightly inward
                matrices.translate(side * -0.02f, 0.02f, 0.0f)
            }
            AnthroModelType.ANTHRO_BASE -> {
                // Basic anthro - neutral positioning
                // No additional transform needed
            }
            AnthroModelType.NONE -> {
                // Default player - no transform
            }
        }
    }
    
    /**
     * Get the bone to render for first-person arm view.
     * This isolates just the arm/paw bones for rendering.
     */
    fun getFirstPersonBones(modelType: AnthroModelType, arm: Arm): List<String> {
        val baseBone = getArmBoneName(arm)
        
        // Return the arm bone and any child bones (fingers, claws, etc.)
        return listOf(baseBone) + when (modelType) {
            AnthroModelType.PROTOGEN -> listOf(
                "${baseBone}_lower",
                "${baseBone}_hand",
                "${baseBone}_fingers"
            )
            AnthroModelType.K9 -> listOf(
                "${baseBone}_lower",
                "${baseBone}_paw",
                "${baseBone}_claws"
            )
            AnthroModelType.FELINE -> listOf(
                "${baseBone}_lower", 
                "${baseBone}_paw",
                "${baseBone}_claws"
            )
            AnthroModelType.ANTHRO_BASE -> listOf(
                "${baseBone}_lower",
                "${baseBone}_hand"
            )
            AnthroModelType.NONE -> emptyList()
        }
    }
}

