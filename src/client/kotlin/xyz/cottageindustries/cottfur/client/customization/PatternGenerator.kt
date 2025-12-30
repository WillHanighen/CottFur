package xyz.cottageindustries.cottfur.client.customization

import xyz.cottageindustries.cottfur.CottfurConstants

/**
 * Defines available patterns and their configurations for anthro model textures.
 * Pattern rendering will be handled through shader-based color tinting
 * rather than direct texture manipulation.
 */
object PatternGenerator {
    
    /**
     * Available pattern types.
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
     * Configuration for pattern generation.
     * These values are stored and sent to the rendering system.
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
     * Blend two colors based on a factor (0.0 = color1, 1.0 = color2).
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
     * Convert a hex color string to an integer.
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
     * Convert an integer color to a hex string.
     */
    fun colorToHex(color: Int): String {
        return String.format("#%06X", color and 0xFFFFFF)
    }
}
