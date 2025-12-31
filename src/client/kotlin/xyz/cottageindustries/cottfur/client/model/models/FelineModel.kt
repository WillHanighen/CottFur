package xyz.cottageindustries.cottfur.client.model.models

import xyz.cottageindustries.cottfur.client.model.AnthroModel
import xyz.cottageindustries.cottfur.client.model.AnthroModelType

/**
 * Model definition for the Feline species.
 * 
 * Represents cats, lions, tigers, and other feline-type anthros with:
 * - Shorter, rounder muzzle compared to canines
 * - Pointed or rounded triangular ears
 * - Long, flexible tail for balance and expression
 * - Whiskers for added detail
 * 
 * ## Unique Bones
 * - [BONE_WHISKERS]: Facial whiskers, can be animated
 */
class FelineModel : AnthroModel(AnthroModelType.FELINE) {
    
    companion object {
        /** Whiskers bone - facial whiskers that can twitch/move. */
        const val BONE_WHISKERS = "whiskers"
    }
}
