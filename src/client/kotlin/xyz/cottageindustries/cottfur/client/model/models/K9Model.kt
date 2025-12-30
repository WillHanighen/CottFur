package xyz.cottageindustries.cottfur.client.model.models

import xyz.cottageindustries.cottfur.client.model.AnthroModel
import xyz.cottageindustries.cottfur.client.model.AnthroModelType

/**
 * Model for the K9 (canine) species - dogs, wolves, foxes, etc.
 * Features include:
 * - Prominent muzzle
 * - Fluffy ears (various shapes based on variant)
 * - Fluffy tail
 */
class K9Model : AnthroModel(AnthroModelType.K9) {
    
    companion object {
        // K9-specific bone names
        const val BONE_MUZZLE = "muzzle"
        const val BONE_JAW = "jaw"
    }
}
