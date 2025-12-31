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
 * 
 * This singleton maintains a mapping of [AnthroModelType] to [AnthroModel] instances,
 * providing centralized access to model definitions. All built-in species are registered
 * during client initialization.
 * 
 * @see AnthroModel
 * @see AnthroModelType
 */
object ModelRegistry {
    
    /** Map of model type to model instance. Populated in [initialize]. */
    private val models = mutableMapOf<AnthroModelType, AnthroModel>()
    
    /** Tracks whether [initialize] has been called to prevent double initialization. */
    private var initialized = false
    
    /**
     * Initializes the model registry with all built-in anthro models.
     * 
     * Must be called once during client initialization ([CottfurClient.onInitializeClient]).
     * Subsequent calls are ignored. Registers: Protogen, K9, Feline, and Anthro Base.
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
     * Registers a model instance for a specific type.
     * 
     * @param type The model type to register (cannot be [AnthroModelType.NONE])
     * @param model The model instance to associate with this type
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
     * Gets the model instance for a given type.
     * 
     * @param type The model type to look up
     * @return The model instance, or `null` if not registered (e.g., [AnthroModelType.NONE])
     */
    fun getModel(type: AnthroModelType): AnthroModel? {
        return models[type]
    }
    
    /**
     * Returns all registered model types.
     * 
     * @return Set of all model types that have been registered
     */
    fun getRegisteredTypes(): Set<AnthroModelType> {
        return models.keys.toSet()
    }
    
    /**
     * Checks if a model type has been registered.
     * 
     * @param type The model type to check
     * @return `true` if the type has a registered model instance
     */
    fun isRegistered(type: AnthroModelType): Boolean {
        return type in models
    }
}

/**
 * Implementation of [AnthroAnimatable] for player entities.
 * 
 * This class provides the GeckoLib 5 animatable interface, wrapping the player's
 * model type and custom texture. Each player with an anthro model gets an instance
 * of this class for animation management.
 * 
 * ## GeckoLib 5.4 Integration
 * - Uses [GeckoLibUtil.createInstanceCache] for animation caching
 * - Registers a main controller for idle animations
 * - Controller uses the new 5.x API (no animatable parameter in handler)
 * 
 * @property modelType The current anthro model type for this player
 * @property customTexture Optional custom texture override
 */
class AnthroPlayerAnimatable(
    private var modelType: AnthroModelType = AnthroModelType.NONE,
    private var customTexture: Identifier? = null
) : AnthroAnimatable {
    
    /** GeckoLib animation instance cache for this animatable. */
    private val cache: AnimatableInstanceCache = GeckoLibUtil.createInstanceCache(this)
    
    /** Pre-built idle animation sequence that loops indefinitely. */
    private val idleAnimation = RawAnimation.begin().thenLoop("animation.anthro.idle")
    
    override fun getCustomTexture(): Identifier? = customTexture
    
    override fun getModelType(): AnthroModelType = modelType
    
    /**
     * Updates the model type for this animatable.
     * 
     * @param type The new model type
     */
    fun setModelType(type: AnthroModelType) {
        this.modelType = type
    }
    
    /**
     * Updates the custom texture for this animatable.
     * 
     * @param texture The new texture identifier, or `null` to use the model's default
     */
    fun setCustomTexture(texture: Identifier?) {
        this.customTexture = texture
    }
    
    /**
     * Registers animation controllers with GeckoLib.
     * 
     * Called by GeckoLib during animation setup. Registers a main controller
     * that defaults to the idle animation.
     * 
     * @param controllers The controller registrar provided by GeckoLib
     */
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
