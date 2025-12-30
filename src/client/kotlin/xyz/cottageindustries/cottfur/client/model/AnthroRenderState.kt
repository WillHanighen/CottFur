package xyz.cottageindustries.cottfur.client.model

import net.minecraft.util.Identifier

/**
 * Simple render state for anthro models.
 * Carries data between the animatable and the renderer.
 */
class AnthroRenderState {
    /**
     * The model type being rendered
     */
    var modelType: AnthroModelType = AnthroModelType.NONE
    
    /**
     * Optional custom texture, null means use default for model type
     */
    var customTexture: Identifier? = null
    
    /**
     * Head pitch for look-at animation
     */
    var headPitch: Float = 0f
    
    /**
     * Head yaw for look-at animation
     */
    var headYaw: Float = 0f
    
    /**
     * Body yaw rotation
     */
    var bodyYaw: Float = 0f
    
    /**
     * Limb swing amount (for walking animation)
     */
    var limbSwingAmount: Float = 0f
    
    /**
     * Limb swing phase (for walking animation)
     */
    var limbSwing: Float = 0f
    
    /**
     * Whether the entity is sprinting
     */
    var isSprinting: Boolean = false
    
    /**
     * Whether the entity is sneaking
     */
    var isSneaking: Boolean = false
    
    /**
     * Whether the entity is swimming
     */
    var isSwimming: Boolean = false
    
    /**
     * Reset state for reuse
     */
    fun reset() {
        modelType = AnthroModelType.NONE
        customTexture = null
        headPitch = 0f
        headYaw = 0f
        bodyYaw = 0f
        limbSwingAmount = 0f
        limbSwing = 0f
        isSprinting = false
        isSneaking = false
        isSwimming = false
    }
}
