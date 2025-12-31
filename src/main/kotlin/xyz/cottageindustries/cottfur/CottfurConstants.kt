package xyz.cottageindustries.cottfur

import net.minecraft.util.Identifier
import org.slf4j.LoggerFactory

/**
 * Constants and utilities shared across the CottFur mod.
 * 
 * This object provides centralized access to mod-wide constants and helper functions
 * that are used by both server and client code.
 */
object CottfurConstants {
    /** The unique mod identifier used in registries and resource paths. */
    const val MOD_ID = "cottfur"
    
    /** Human-readable mod name for logging and display purposes. */
    const val MOD_NAME = "CottFur"
    
    /** 
     * Shared logger instance for all CottFur logging.
     * 
     * Annotated with @JvmField for Java interop in mixins.
     */
    @JvmField
    val LOGGER = LoggerFactory.getLogger(MOD_NAME)
    
    /**
     * Creates a namespaced [Identifier] for this mod.
     * 
     * @param path The resource path (e.g., "textures/entity/protogen.png")
     * @return An Identifier with namespace "cottfur" and the given path
     */
    fun id(path: String): Identifier = Identifier.of(MOD_ID, path)
}

