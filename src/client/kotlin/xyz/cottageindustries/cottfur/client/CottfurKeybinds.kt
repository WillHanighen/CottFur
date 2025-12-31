package xyz.cottageindustries.cottfur.client

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import org.lwjgl.glfw.GLFW
import xyz.cottageindustries.cottfur.CottfurConstants
import xyz.cottageindustries.cottfur.client.ui.AnthroCustomizationScreen

/**
 * Handles keybind registration and processing for CottFur.
 * 
 * Registered keybinds:
 * - **G** (default): Opens the anthro model customization screen
 * 
 * Keybinds are processed every client tick via [ClientTickEvents.END_CLIENT_TICK].
 */
object CottfurKeybinds {
    
    /** The keybind for opening the customization screen. Lateinit, set in [register]. */
    private lateinit var openCustomizationKey: KeyBinding
    
    /**
     * Registers all CottFur keybinds with Fabric's keybind system.
     * 
     * Must be called during client initialization ([CottfurClient.onInitializeClient]).
     * Sets up a tick event listener to poll key states and trigger actions.
     */
    fun register() {
        CottfurConstants.LOGGER.info("Registering CottFur keybinds...")
        
        // Key to open customization screen (default: G)
        // Using the MISC category which is a standard Minecraft category
        openCustomizationKey = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "key.cottfur.open_customization",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                KeyBinding.Category.MISC
            )
        )
        
        // Register tick event to process keybinds
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            while (openCustomizationKey.wasPressed()) {
                // Only open if not already in a screen
                if (client.currentScreen == null) {
                    AnthroCustomizationScreen.open()
                }
            }
        }
        
        CottfurConstants.LOGGER.info("CottFur keybinds registered!")
    }
}

