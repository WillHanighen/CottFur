package xyz.cottageindustries.cottfur.client.model

import net.minecraft.util.Identifier
import software.bernie.geckolib.animatable.GeoAnimatable
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache
import software.bernie.geckolib.animatable.manager.AnimatableManager
import software.bernie.geckolib.animation.AnimationController
import software.bernie.geckolib.animation.RawAnimation
import software.bernie.geckolib.animation.`object`.PlayState
import software.bernie.geckolib.util.GeckoLibUtil
import xyz.cottageindustries.cottfur.CottfurConstants
import xyz.cottageindustries.cottfur.client.model.models.*

/**
 * Registry for managing and accessing anthro models.
 * This singleton provides access to model instances for each anthro type.
 */
object ModelRegistry {
    
    private val models = mutableMapOf<AnthroModelType, AnthroModel>()
    private var initialized = false
    
    /**
     * Initialize the model registry.
     * Should be called during client initialization.
     */
    fun initialize() {
        if (initialized) return
        
        CottfurConstants.LOGGER.info("Initializing CottFur model registry...")
        
        // Register all built-in models
        registerModel(AnthroModelType.PROTOGEN, ProtogenModel())
        registerModel(AnthroModelType.K9, K9Model())
        registerModel(AnthroModelType.FELINE, FelineModel())
        registerModel(AnthroModelType.ANTHRO_BASE, AnthroBaseModel())
        
        CottfurConstants.LOGGER.info("Registered ${models.size} anthro models")
        initialized = true
    }
    
    /**
     * Register a model for a specific type
     */
    private fun registerModel(type: AnthroModelType, model: AnthroModel) {
        if (type == AnthroModelType.NONE) {
            CottfurConstants.LOGGER.warn("Cannot register model for NONE type")
            return
        }
        models[type] = model
        CottfurConstants.LOGGER.debug("Registered model for type: ${type.displayName}")
    }
    
    /**
     * Get a model by its type
     */
    fun getModel(type: AnthroModelType): AnthroModel? {
        return models[type]
    }
    
    /**
     * Get all registered model types
     */
    fun getRegisteredTypes(): Set<AnthroModelType> {
        return models.keys.toSet()
    }
    
    /**
     * Check if a model type is registered
     */
    fun isRegistered(type: AnthroModelType): Boolean {
        return type in models
    }
}

/**
 * Implementation of AnthroAnimatable for player entities.
 * This class wraps player data and provides the interface needed by GeckoLib 5.
 */
class AnthroPlayerAnimatable(
    private var modelType: AnthroModelType = AnthroModelType.NONE,
    private var customTexture: Identifier? = null
) : AnthroAnimatable {
    
    private val cache: AnimatableInstanceCache = GeckoLibUtil.createInstanceCache(this)
    
    // Idle animation for default state
    private val idleAnimation = RawAnimation.begin().thenLoop("animation.anthro.idle")
    
    override fun getCustomTexture(): Identifier? = customTexture
    
    override fun getModelType(): AnthroModelType = modelType
    
    fun setModelType(type: AnthroModelType) {
        this.modelType = type
    }
    
    fun setCustomTexture(texture: Identifier?) {
        this.customTexture = texture
    }
    
    override fun registerControllers(controllers: AnimatableManager.ControllerRegistrar) {
        // GeckoLib 5 API: controller takes name, transition ticks, and handler lambda (no animatable!)
        controllers.add(AnimationController<AnthroPlayerAnimatable>("main_controller", 10) { state ->
            // Default to idle animation
            state.setAndContinue(idleAnimation)
        })
    }
    
    override fun getAnimatableInstanceCache(): AnimatableInstanceCache {
        return cache
    }
}
