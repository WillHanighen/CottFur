package xyz.cottageindustries.cottfur.client.model.models

import xyz.cottageindustries.cottfur.client.model.AnthroModel
import xyz.cottageindustries.cottfur.client.model.AnthroModelType

/**
 * Basic anthro model that serves as a template for custom models.
 * This is a simple bipedal anthro with basic features:
 * - Human-like body proportions
 * - Simple round ears
 * - Basic tail
 * 
 * Users can customize this base model with their own textures.
 */
class AnthroBaseModel : AnthroModel(AnthroModelType.ANTHRO_BASE)
