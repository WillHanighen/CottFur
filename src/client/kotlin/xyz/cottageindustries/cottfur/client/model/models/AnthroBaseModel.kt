package xyz.cottageindustries.cottfur.client.model.models

import xyz.cottageindustries.cottfur.client.model.AnthroModel
import xyz.cottageindustries.cottfur.client.model.AnthroModelType

/**
 * Basic/generic anthro model serving as a template for custom designs.
 * 
 * This model provides a simple bipedal anthro base with:
 * - Human-like body proportions
 * - Simple round ears
 * - Basic tail
 * - Standard bone hierarchy without species-specific additions
 * 
 * Intended for users who want to customize via textures without species-specific features.
 * No additional unique bones beyond the standard set in [AnthroModel].
 */
class AnthroBaseModel : AnthroModel(AnthroModelType.ANTHRO_BASE)
