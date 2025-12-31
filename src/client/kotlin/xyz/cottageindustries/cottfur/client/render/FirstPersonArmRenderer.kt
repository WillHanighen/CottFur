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
 * Utilities for first-person anthro arm/paw rendering.
 * 
 * This object provides transformation and utility functions for rendering
 * custom anthro arms in first-person view, including:
 * - Arm positioning and swing animation math
 * - Species-specific paw transform adjustments
 * - Bone name lookup for arm isolation
 * 
 * @see AnthroArmRenderer for the main rendering entry point
 */
object FirstPersonArmRenderer {
    
    /**
     * Data container for first-person arm rendering parameters.
     * 
     * @property modelType The anthro species being rendered
     * @property customTexture Optional custom texture override
     * @property arm Which arm (LEFT or RIGHT)
     * @property swingProgress Current arm swing progress (0.0-1.0)
     * @property equippedProgress Item equip animation progress
     */
    data class ArmRenderInfo(
        val modelType: AnthroModelType,
        val customTexture: Identifier?,
        val arm: Arm,
        val swingProgress: Float,
        val equippedProgress: Float
    )
    
    /**
     * Checks if custom first-person arms should be rendered.
     * 
     * @return `true` if the local player has an anthro model active
     */
    fun shouldRenderCustomArm(): Boolean {
        val client = MinecraftClient.getInstance()
        val player = client.player ?: return false
        
        // Check if player has an anthro model active
        // TODO: Replace with actual player data lookup when data sync is implemented
        return true // For now, always render custom arms for testing
    }
    
    /**
     * Gets the texture to use for arm rendering.
     * 
     * @param modelType The anthro species
     * @param customTexture Optional custom texture override
     * @return The custom texture if provided, otherwise the model's default texture
     */
    fun getArmTexture(modelType: AnthroModelType, customTexture: Identifier?): Identifier {
        return customTexture ?: modelType.getDefaultTextureLocation()
    }
    
    /**
     * Gets the bone name for an arm based on handedness.
     * 
     * @param arm The arm side (LEFT or RIGHT)
     * @return The corresponding bone name from [AnthroModel]
     */
    fun getArmBoneName(arm: Arm): String {
        return when (arm) {
            Arm.LEFT -> AnthroModel.BONE_LEFT_ARM
            Arm.RIGHT -> AnthroModel.BONE_RIGHT_ARM
        }
    }
    
    /**
     * Applies vanilla-style first-person arm transformations.
     * 
     * Positions the arm in first-person view with swing animation.
     * Based on vanilla arm positioning, adapted for anthro paws.
     * 
     * @param matrices The matrix stack to apply transforms to
     * @param arm Which arm (affects left/right mirroring)
     * @param swingProgress Current swing animation progress (0.0-1.0)
     * @param equippedProgress Item equip animation progress
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
     * Applies species-specific paw positioning adjustments.
     * 
     * Different anthro species have different paw shapes and may need
     * slight positional adjustments for natural appearance.
     * 
     * @param matrices The matrix stack to apply transforms to
     * @param modelType The anthro species (affects positioning)
     * @param arm Which arm (affects left/right mirroring)
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
     * Gets the list of bone names needed for first-person arm rendering.
     * 
     * Returns the arm bone and all child bones (lower arm, hand/paw, fingers/claws).
     * Used to isolate just the arm portion of the full model for first-person view.
     * 
     * @param modelType The anthro species (affects which child bones exist)
     * @param arm Which arm (LEFT or RIGHT)
     * @return List of bone names to render, starting with the upper arm
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

