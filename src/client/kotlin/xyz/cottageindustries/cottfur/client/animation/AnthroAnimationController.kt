package xyz.cottageindustries.cottfur.client.animation

import software.bernie.geckolib.animation.AnimationController
import software.bernie.geckolib.animation.RawAnimation
import software.bernie.geckolib.animation.`object`.PlayState
import xyz.cottageindustries.cottfur.client.model.AnthroAnimatable

/**
 * Manages animations for anthro player models.
 * 
 * This object provides:
 * - Animation name constants matching the `.animation.json` files
 * - Pre-built [RawAnimation] sequences for common states
 * - Factory methods for creating GeckoLib animation controllers
 * - Speed thresholds for animation state transitions
 * 
 * ## Animation Files
 * Animation names must match those defined in `assets/cottfur/animations/`.
 * Each species has its own animation file with the same animation names.
 */
object AnthroAnimationController {
    
    // ========== Animation Name Constants ==========
    // These must match the animation names in .animation.json files
    
    /** Idle standing animation, loops continuously. */
    const val ANIM_IDLE = "animation.anthro.idle"
    
    /** Walking animation, loops while moving slowly. */
    const val ANIM_WALK = "animation.anthro.walk"
    
    /** Running/sprinting animation, loops while moving fast. */
    const val ANIM_RUN = "animation.anthro.run"
    
    /** Tail wagging emote, plays once. */
    const val ANIM_TAIL_WAG = "animation.anthro.tail_wag"
    
    /** Ear flicking emote, plays once. */
    const val ANIM_EAR_FLICK = "animation.anthro.ear_flick"
    
    // ========== Pre-built Animation Sequences ==========
    
    /** Idle animation that loops indefinitely. */
    val IDLE_ANIM: RawAnimation = RawAnimation.begin().thenLoop(ANIM_IDLE)
    
    /** Walk animation that loops indefinitely. */
    val WALK_ANIM: RawAnimation = RawAnimation.begin().thenLoop(ANIM_WALK)
    
    /** Run animation that loops indefinitely. */
    val RUN_ANIM: RawAnimation = RawAnimation.begin().thenLoop(ANIM_RUN)
    
    /** Tail wag animation that plays once. */
    val TAIL_WAG_ANIM: RawAnimation = RawAnimation.begin().thenPlay(ANIM_TAIL_WAG)
    
    /** Ear flick animation that plays once. */
    val EAR_FLICK_ANIM: RawAnimation = RawAnimation.begin().thenPlay(ANIM_EAR_FLICK)
    
    // ========== Speed Thresholds ==========
    
    /** Movement speed threshold to switch from idle to walk animation. */
    const val WALK_THRESHOLD = 0.1
    
    /** Movement speed threshold to switch from walk to run animation. */
    const val RUN_THRESHOLD = 0.5
    
    /**
     * Creates the main animation controller for movement-based animations.
     * 
     * Handles automatic switching between idle and walk animations based on
     * the entity's movement state.
     * 
     * ## GeckoLib 5.x API
     * Uses the new constructor signature: `(name, transitionTicks, handler)`.
     * The handler lambda does NOT receive an animatable parameter (change from 4.x).
     * 
     * @param T The animatable type, must implement [AnthroAnimatable]
     * @return A configured animation controller for movement animations
     */
    fun <T : AnthroAnimatable> createMainController(): AnimationController<T> {
        return AnimationController("main_controller", 5) { state ->
            // Check if moving and set appropriate animation
            if (state.isMoving) {
                state.setAndContinue(WALK_ANIM)
            } else {
                state.setAndContinue(IDLE_ANIM)
            }
        }
    }
    
    /**
     * Creates a secondary animation controller for overlay animations.
     * 
     * Handles animations that play on top of the main movement animations,
     * such as tail wagging and ear flicks.
     * 
     * @param T The animatable type, must implement [AnthroAnimatable]
     * @return A configured animation controller for overlay animations
     */
    fun <T : AnthroAnimatable> createSecondaryController(): AnimationController<T> {
        return AnimationController("secondary_controller", 3) { _ ->
            // Secondary animations - just continue current state
            PlayState.CONTINUE
        }
    }
}

/**
 * Emote animations that can be triggered by players.
 * 
 * Each emote has:
 * - An animation name matching the `.animation.json` files
 * - A display name for the UI
 * - A duration in seconds (-1 means loops until cancelled)
 * 
 * @property animationName The GeckoLib animation name
 * @property displayName Human-readable name for UI display
 * @property duration Duration in seconds, or -1 for looping emotes
 */
enum class AnthroEmote(
    val animationName: String,
    val displayName: String,
    val duration: Float
) {
    WAVE("animation.anthro.wave", "Wave", 1.5f),
    SIT("animation.anthro.sit", "Sit", -1f), // -1 = until cancelled
    DANCE("animation.anthro.dance", "Dance", 3.0f),
    NOD("animation.anthro.nod", "Nod", 0.5f),
    SHAKE("animation.anthro.shake", "Shake Head", 0.5f),
    BOW("animation.anthro.bow", "Bow", 1.0f);
    
    /**
     * Converts this emote to a GeckoLib [RawAnimation].
     * 
     * @return A looping animation if [duration] < 0, otherwise a one-shot animation
     */
    fun toRawAnimation(): RawAnimation {
        return if (duration < 0) {
            RawAnimation.begin().thenLoop(animationName)
        } else {
            RawAnimation.begin().thenPlay(animationName)
        }
    }
}

/**
 * Registry for managing emote animations.
 * 
 * Provides lookup of emotes by name and enumeration of all available emotes.
 * Used by the emote selection UI and keybind system.
 */
object EmoteRegistry {
    /** Map of lowercase emote names to emote instances for fast lookup. */
    private val emotes = AnthroEmote.entries.associateBy { it.name.lowercase() }
    
    /**
     * Looks up an emote by its name (case-insensitive).
     * 
     * @param name The emote name (e.g., "wave", "dance")
     * @return The matching [AnthroEmote], or `null` if not found
     */
    fun getEmote(name: String): AnthroEmote? {
        return emotes[name.lowercase()]
    }
    
    /**
     * Returns all available emotes.
     * 
     * @return List of all [AnthroEmote] entries
     */
    fun getAllEmotes(): List<AnthroEmote> {
        return AnthroEmote.entries
    }
}
