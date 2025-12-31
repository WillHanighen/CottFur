package xyz.cottageindustries.cottfur.client.model.models

import xyz.cottageindustries.cottfur.client.model.AnthroModel
import xyz.cottageindustries.cottfur.client.model.AnthroModelType

/**
 * Model definition for the Protogen species.
 * 
 * Protogens are robotic/cybernetic anthros characterized by:
 * - LED visor face for displaying expressions and emotions
 * - Mechanical antenna ears
 * - Tech-styled body with circuit patterns and armored plates
 * 
 * ## Unique Bones
 * - [BONE_VISOR]: The LED face display
 * - [BONE_LEFT_ANTENNA] / [BONE_RIGHT_ANTENNA]: Mechanical ear antennae
 */
class ProtogenModel : AnthroModel(AnthroModelType.PROTOGEN) {
    
    companion object {
        /** Visor bone - the LED face display, can be animated for expressions. */
        const val BONE_VISOR = "visor"
        
        /** Left antenna bone - mechanical ear on left side. */
        const val BONE_LEFT_ANTENNA = "left_antenna"
        
        /** Right antenna bone - mechanical ear on right side. */
        const val BONE_RIGHT_ANTENNA = "right_antenna"
    }
}
