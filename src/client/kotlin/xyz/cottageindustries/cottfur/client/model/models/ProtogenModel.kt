package xyz.cottageindustries.cottfur.client.model.models

import xyz.cottageindustries.cottfur.client.model.AnthroModel
import xyz.cottageindustries.cottfur.client.model.AnthroModelType

/**
 * Model for the Protogen species - a robotic anthro with a visor face.
 * Features include:
 * - LED visor for displaying expressions
 * - Mechanical ear antenna
 * - Tech-styled body with circuit patterns
 */
class ProtogenModel : AnthroModel(AnthroModelType.PROTOGEN) {
    
    companion object {
        // Protogen-specific bone names
        const val BONE_VISOR = "visor"
        const val BONE_LEFT_ANTENNA = "left_antenna"
        const val BONE_RIGHT_ANTENNA = "right_antenna"
    }
}
