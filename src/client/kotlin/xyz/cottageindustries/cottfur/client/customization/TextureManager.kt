package xyz.cottageindustries.cottfur.client.customization

import net.minecraft.client.MinecraftClient
import net.minecraft.util.Identifier
import xyz.cottageindustries.cottfur.CottfurConstants
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID

/**
 * Manages custom textures uploaded by players.
 * 
 * This singleton handles:
 * - Validation of uploaded texture files (size, format, dimensions)
 * - Storage of textures in the config directory
 * - Tracking of registered texture IDs
 * - Retrieval of stored textures
 * 
 * ## Storage Location
 * Textures are stored in: `{minecraft}/config/cottfur/textures/`
 * 
 * ## Constraints
 * - Format: PNG only
 * - Max file size: 1MB
 * - Expected dimensions: 64×64 pixels (standard player skin size)
 */
object TextureManager {
    
    /** 
     * Directory for storing custom textures.
     * Created lazily on first access.
     */
    private val CUSTOM_TEXTURE_DIR: Path by lazy {
        val configDir = MinecraftClient.getInstance().runDirectory.toPath()
            .resolve("config")
            .resolve("cottfur")
            .resolve("textures")
        Files.createDirectories(configDir)
        configDir
    }
    
    // Cache of registered texture IDs
    private val registeredTextures = mutableSetOf<String>()
    
    // Expected texture dimensions (standard player skin size)
    private const val EXPECTED_WIDTH = 64
    private const val EXPECTED_HEIGHT = 64
    
    // Maximum file size (1MB)
    private const val MAX_FILE_SIZE = 1024 * 1024L
    
    /**
     * Result of a texture import operation.
     * 
     * Sealed class with two variants:
     * - [Success]: Contains the generated texture ID and resource identifier
     * - [Error]: Contains an error message explaining the failure
     */
    sealed class TextureResult {
        /** Successful import with generated texture ID and resource identifier. */
        data class Success(val textureId: String, val identifier: Identifier) : TextureResult()
        
        /** Failed import with human-readable error message. */
        data class Error(val message: String) : TextureResult()
    }
    
    /**
     * Validates and imports a custom texture file.
     * 
     * Validates the file (existence, size, format), generates a unique ID,
     * copies to the storage directory, and returns the resource identifier.
     * 
     * @param file The PNG file to import
     * @return [TextureResult.Success] with texture ID and identifier, or [TextureResult.Error] with message
     */
    fun loadCustomTexture(file: File): TextureResult {
        // Validate file exists and is readable
        if (!file.exists() || !file.canRead()) {
            return TextureResult.Error("File does not exist or cannot be read")
        }
        
        // Check file size
        if (file.length() > MAX_FILE_SIZE) {
            return TextureResult.Error("File too large (max 1MB)")
        }
        
        // Check file extension
        if (!file.name.lowercase().endsWith(".png")) {
            return TextureResult.Error("Only PNG files are supported")
        }
        
        try {
            // Generate unique texture ID
            val textureId = UUID.randomUUID().toString().take(8)
            
            // Save texture to local storage
            val savedFile = CUSTOM_TEXTURE_DIR.resolve("$textureId.png").toFile()
            file.copyTo(savedFile, overwrite = true)
            
            // Create identifier for this texture
            val identifier = CottfurConstants.id("textures/custom/$textureId")
            registeredTextures.add(textureId)
            
            CottfurConstants.LOGGER.info("Saved custom texture: $textureId")
            
            return TextureResult.Success(textureId, identifier)
            
        } catch (e: Exception) {
            CottfurConstants.LOGGER.error("Failed to load texture: ${e.message}")
            return TextureResult.Error("Failed to load texture: ${e.message}")
        }
    }
    
    /**
     * Gets the file system path for a stored texture.
     * 
     * @param textureId The texture ID to look up
     * @return The file if it exists, or `null` if not found
     */
    fun getStoredTexturePath(textureId: String): File? {
        val file = CUSTOM_TEXTURE_DIR.resolve("$textureId.png").toFile()
        return if (file.exists()) file else null
    }
    
    /**
     * Creates a resource identifier for a stored custom texture.
     * 
     * @param textureId The texture ID
     * @return Identifier in the format `cottfur:textures/custom/{textureId}`
     */
    fun getTextureIdentifier(textureId: String): Identifier {
        return CottfurConstants.id("textures/custom/$textureId")
    }
    
    /**
     * Checks if a texture with the given ID exists in storage.
     * 
     * @param textureId The texture ID to check
     * @return `true` if the texture file exists
     */
    fun textureExists(textureId: String): Boolean {
        return CUSTOM_TEXTURE_DIR.resolve("$textureId.png").toFile().exists()
    }
    
    /**
     * Deletes a custom texture from storage.
     * 
     * @param textureId The texture ID to delete
     * @return `true` if the file was deleted, `false` if it didn't exist
     */
    fun deleteTexture(textureId: String): Boolean {
        val file = CUSTOM_TEXTURE_DIR.resolve("$textureId.png").toFile()
        
        registeredTextures.remove(textureId)
        
        return if (file.exists()) {
            file.delete()
        } else {
            false
        }
    }
    
    /**
     * Lists all stored custom texture IDs.
     * 
     * Scans the texture storage directory for PNG files.
     * 
     * @return List of texture IDs (filenames without extension)
     */
    fun listStoredTextures(): List<String> {
        return CUSTOM_TEXTURE_DIR.toFile()
            .listFiles { file -> file.extension.lowercase() == "png" }
            ?.map { it.nameWithoutExtension }
            ?: emptyList()
    }
}

