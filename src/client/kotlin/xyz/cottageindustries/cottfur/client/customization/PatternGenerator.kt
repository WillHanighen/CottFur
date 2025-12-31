package xyz.cottageindustries.cottfur.client.customization

import xyz.cottageindustries.cottfur.CottfurConstants

/**
 * Defines available fur patterns and color utilities for anthro model textures.
 * 
 * This object provides:
 * - Enum of available pattern types with display names
 * - Pattern configuration data class
 * - Color manipulation utilities (blending, hex conversion)
 * 
 * ## Implementation Note
 * Pattern rendering is designed to use shader-based color tinting rather than
 * direct texture manipulation, allowing dynamic color changes without texture regeneration.
 */
object PatternGenerator {
    
    /**
     * Available fur pattern types.
     * 
     * Each pattern has a display name for UI and a string ID for storage/networking.
     * 
     * @property displayName Human-readable name for the customization UI
     * @property id Unique string identifier for config storage
     */
    enum class PatternType(val displayName: String, val id: String) {
        NONE("No Pattern", "none"),
        STRIPES("Stripes", "stripes"),
        SPOTS("Spots", "spots"),
        GRADIENT("Gradient", "gradient"),
        TWO_TONE("Two-Tone", "two_tone"),
        TIGER("Tiger Stripes", "tiger"),
        TABBY("Tabby", "tabby"),
        HUSKY("Husky Markings", "husky"),
        CALICO("Calico", "calico");
        
        companion object {
            fun fromId(id: String): PatternType {
                return entries.find { it.id.equals(id, ignoreCase = true) } ?: NONE
            }
            
            fun all(): List<PatternType> = entries
        }
    }
    
    /**
     * Configuration for pattern generation and rendering.
     * 
     * Contains all parameters needed to render a pattern on an anthro model,
     * including colors, pattern type, and rendering parameters.
     * 
     * @property type The pattern type to apply
     * @property primaryColor Primary body color as packed RGB
     * @property secondaryColor Secondary marking color as packed RGB
     * @property accentColor Accent highlight color as packed RGB
     * @property intensity Pattern visibility (0.0 = invisible, 1.0 = full)
     * @property scale Pattern size multiplier (1.0 = default)
     */
    data class PatternConfig(
        val type: PatternType = PatternType.NONE,
        val primaryColor: Int = 0xFFFFFF,
        val secondaryColor: Int = 0x888888,
        val accentColor: Int = 0xFF0000,
        val intensity: Float = 0.5f, // 0.0 to 1.0
        val scale: Float = 1.0f // Pattern scale multiplier
    ) {
        /**
         * Convert colors to normalized RGB floats for shader use.
         */
        fun getPrimaryColorFloat(): FloatArray {
            return intColorToFloat(primaryColor)
        }
        
        fun getSecondaryColorFloat(): FloatArray {
            return intColorToFloat(secondaryColor)
        }
        
        fun getAccentColorFloat(): FloatArray {
            return intColorToFloat(accentColor)
        }
        
        private fun intColorToFloat(color: Int): FloatArray {
            val r = ((color shr 16) and 0xFF) / 255f
            val g = ((color shr 8) and 0xFF) / 255f
            val b = (color and 0xFF) / 255f
            return floatArrayOf(r, g, b)
        }
        
        companion object {
            val DEFAULT = PatternConfig()
        }
    }
    
    /**
     * Linearly interpolates between two RGB colors.
     * 
     * @param color1 The starting color (returned when factor = 0.0)
     * @param color2 The ending color (returned when factor = 1.0)
     * @param factor Blend factor between 0.0 and 1.0
     * @return The blended color as a packed RGB integer
     */
    fun blendColors(color1: Int, color2: Int, factor: Float): Int {
        val r1 = (color1 shr 16) and 0xFF
        val g1 = (color1 shr 8) and 0xFF
        val b1 = color1 and 0xFF
        
        val r2 = (color2 shr 16) and 0xFF
        val g2 = (color2 shr 8) and 0xFF
        val b2 = color2 and 0xFF
        
        val r = (r1 + (r2 - r1) * factor).toInt()
        val g = (g1 + (g2 - g1) * factor).toInt()
        val b = (b1 + (b2 - b1) * factor).toInt()
        
        return (r shl 16) or (g shl 8) or b
    }
    
    /**
     * Parses a hex color string to a packed RGB integer.
     * 
     * Accepts formats: "#RRGGBB", "0xRRGGBB", or "RRGGBB"
     * 
     * @param hex The hex color string
     * @return The parsed color, or 0xFFFFFF (white) if parsing fails
     */
    fun parseColor(hex: String): Int {
        val cleanHex = hex.removePrefix("#").removePrefix("0x")
        return try {
            cleanHex.toInt(16)
        } catch (e: NumberFormatException) {
            CottfurConstants.LOGGER.warn("Invalid color: $hex")
            0xFFFFFF
        }
    }
    
    /**
     * Converts a packed RGB integer to a hex color string.
     * 
     * @param color The color as packed RGB (0xRRGGBB)
     * @return Hex string in format "#RRGGBB"
     */
    fun colorToHex(color: Int): String {
        return String.format("#%06X", color and 0xFFFFFF)
    }
}
