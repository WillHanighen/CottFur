package xyz.cottageindustries.cottfur.client.animation

import software.bernie.geckolib.animation.AnimationController
import software.bernie.geckolib.animation.RawAnimation
import software.bernie.geckolib.animation.`object`.PlayState
import xyz.cottageindustries.cottfur.client.model.AnthroAnimatable

/**
 * Manages animations for anthro player models.
 * Handles switching between different animation states based on player movement and actions.
 */
object AnthroAnimationController {
    
    // Animation names - must match the animation.json files
    const val ANIM_IDLE = "animation.anthro.idle"
    const val ANIM_WALK = "animation.anthro.walk"
    const val ANIM_RUN = "animation.anthro.run"
    const val ANIM_TAIL_WAG = "animation.anthro.tail_wag"
    const val ANIM_EAR_FLICK = "animation.anthro.ear_flick"
    
    // Pre-built animation sequences
    val IDLE_ANIM: RawAnimation = RawAnimation.begin().thenLoop(ANIM_IDLE)
    val WALK_ANIM: RawAnimation = RawAnimation.begin().thenLoop(ANIM_WALK)
    val RUN_ANIM: RawAnimation = RawAnimation.begin().thenLoop(ANIM_RUN)
    val TAIL_WAG_ANIM: RawAnimation = RawAnimation.begin().thenPlay(ANIM_TAIL_WAG)
    val EAR_FLICK_ANIM: RawAnimation = RawAnimation.begin().thenPlay(ANIM_EAR_FLICK)
    
    // Speed thresholds for animation switching
    const val WALK_THRESHOLD = 0.1
    const val RUN_THRESHOLD = 0.5
    
    /**
     * Create a main animation controller that handles movement-based animations.
     * Uses GeckoLib 5 API - the controller name and transition ticks come first, handler last.
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
     * Create a secondary controller for overlay animations (tail wag, ear flicks).
     */
    fun <T : AnthroAnimatable> createSecondaryController(): AnimationController<T> {
        return AnimationController("secondary_controller", 3) { _ ->
            // Secondary animations - just continue current state
            PlayState.CONTINUE
        }
    }
}

/**
 * Emote definitions that can be triggered by players.
 */
enum class AnthroEmote(
    val animationName: String,
    val displayName: String,
    val duration: Float // in seconds
) {
    WAVE("animation.anthro.wave", "Wave", 1.5f),
    SIT("animation.anthro.sit", "Sit", -1f), // -1 = until cancelled
    DANCE("animation.anthro.dance", "Dance", 3.0f),
    NOD("animation.anthro.nod", "Nod", 0.5f),
    SHAKE("animation.anthro.shake", "Shake Head", 0.5f),
    BOW("animation.anthro.bow", "Bow", 1.0f);
    
    fun toRawAnimation(): RawAnimation {
        return if (duration < 0) {
            RawAnimation.begin().thenLoop(animationName)
        } else {
            RawAnimation.begin().thenPlay(animationName)
        }
    }
}

/**
 * Registry for managing emote animations and their keybinds.
 */
object EmoteRegistry {
    private val emotes = AnthroEmote.entries.associateBy { it.name.lowercase() }
    
    /**
     * Get an emote by name.
     */
    fun getEmote(name: String): AnthroEmote? {
        return emotes[name.lowercase()]
    }
    
    /**
     * Get all available emotes.
     */
    fun getAllEmotes(): List<AnthroEmote> {
        return AnthroEmote.entries
    }
}
