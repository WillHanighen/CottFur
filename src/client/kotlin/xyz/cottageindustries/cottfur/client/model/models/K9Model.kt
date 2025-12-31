package xyz.cottageindustries.cottfur.client.model.models

import xyz.cottageindustries.cottfur.client.model.AnthroModel
import xyz.cottageindustries.cottfur.client.model.AnthroModelType

/**
 * Model definition for the K9 (canine) species.
 * 
 * Represents dogs, wolves, foxes, and other canine-type anthros with:
 * - Prominent muzzle with articulated jaw
 * - Fluffy, expressive ears (shapes vary by variant)
 * - Fluffy wagging tail
 * 
 * ## Unique Bones
 * - [BONE_MUZZLE]: The snout/nose area
 * - [BONE_JAW]: Lower jaw for mouth animations
 */
class K9Model : AnthroModel(AnthroModelType.K9) {
    
    companion object {
        /** Muzzle bone - the snout/nose structure. */
        const val BONE_MUZZLE = "muzzle"
        
        /** Jaw bone - lower jaw for mouth open/close animations. */
        const val BONE_JAW = "jaw"
    }
}
