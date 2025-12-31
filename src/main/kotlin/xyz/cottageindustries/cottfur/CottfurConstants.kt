package xyz.cottageindustries.cottfur

import net.minecraft.util.Identifier
import org.slf4j.LoggerFactory

/**
 * Constants and utilities shared across the CottFur mod
 */
object CottfurConstants {
    const val MOD_ID = "cottfur"
    const val MOD_NAME = "CottFur"
    
    //@JvmField
    @JvmField
    val LOGGER = LoggerFactory.getLogger(MOD_NAME)
    
    /**
     * Create an identifier for this mod
     */
    fun id(path: String): Identifier = Identifier.of(MOD_ID, path)
}

