package xyz.cottageindustries.cottfur.client.model.models

import xyz.cottageindustries.cottfur.client.model.AnthroModel
import xyz.cottageindustries.cottfur.client.model.AnthroModelType

/**
 * Model for the Feline species - cats, lions, tigers, etc.
 * Features include:
 * - Shorter muzzle
 * - Pointed or rounded ears
 * - Long flexible tail
 */
class FelineModel : AnthroModel(AnthroModelType.FELINE) {
    
    companion object {
        // Feline-specific bone names
        const val BONE_WHISKERS = "whiskers"
    }
}
