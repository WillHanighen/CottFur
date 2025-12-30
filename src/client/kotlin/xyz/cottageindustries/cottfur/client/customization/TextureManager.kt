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
 * Handles validation, storage, and tracking of custom texture files.
 */
object TextureManager {
    
    // Directory for storing custom textures
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
     * Result of a texture validation/load operation.
     */
    sealed class TextureResult {
        data class Success(val textureId: String, val identifier: Identifier) : TextureResult()
        data class Error(val message: String) : TextureResult()
    }
    
    /**
     * Validate and copy a custom texture file.
     * 
     * @param file The PNG file to load
     * @return TextureResult indicating success or failure
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
     * Get the file path for a stored texture.
     */
    fun getStoredTexturePath(textureId: String): File? {
        val file = CUSTOM_TEXTURE_DIR.resolve("$textureId.png").toFile()
        return if (file.exists()) file else null
    }
    
    /**
     * Get the identifier for a stored custom texture.
     */
    fun getTextureIdentifier(textureId: String): Identifier {
        return CottfurConstants.id("textures/custom/$textureId")
    }
    
    /**
     * Check if a texture exists.
     */
    fun textureExists(textureId: String): Boolean {
        return CUSTOM_TEXTURE_DIR.resolve("$textureId.png").toFile().exists()
    }
    
    /**
     * Delete a custom texture.
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
     * List all stored custom textures.
     */
    fun listStoredTextures(): List<String> {
        return CUSTOM_TEXTURE_DIR.toFile()
            .listFiles { file -> file.extension.lowercase() == "png" }
            ?.map { it.nameWithoutExtension }
            ?: emptyList()
    }
}

