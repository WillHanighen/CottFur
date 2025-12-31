package xyz.cottageindustries.cottfur.client.model

import net.minecraft.util.Identifier

/**
 * Mutable render state container for anthro models.
 * 
 * This class carries data from the player entity to the renderer, including:
 * - Model type and texture information
 * - Head/body rotation for look-at animations
 * - Limb swing values for walking/running animations
 * - State flags (sprinting, sneaking, swimming)
 * 
 * Instances are typically reused and [reset] between renders to avoid allocations.
 */
class AnthroRenderState {
    /** The anthro species being rendered. */
    var modelType: AnthroModelType = AnthroModelType.NONE
    
    /** Custom texture override, or `null` to use the model type's default texture. */
    var customTexture: Identifier? = null
    
    /** Head pitch (up/down) in degrees for look-at animation. */
    var headPitch: Float = 0f
    
    /** Head yaw (left/right) in degrees for look-at animation. */
    var headYaw: Float = 0f
    
    /** Body yaw rotation in degrees. */
    var bodyYaw: Float = 0f
    
    /** Limb swing amount (0.0 = still, higher = faster movement). */
    var limbSwingAmount: Float = 0f
    
    /** Limb swing phase for animation cycling. */
    var limbSwing: Float = 0f
    
    /** Whether the entity is currently sprinting. */
    var isSprinting: Boolean = false
    
    /** Whether the entity is currently sneaking/crouching. */
    var isSneaking: Boolean = false
    
    /** Whether the entity is currently swimming. */
    var isSwimming: Boolean = false
    
    /**
     * Resets all fields to default values for reuse.
     * 
     * Call this before populating with new entity data to ensure clean state.
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
