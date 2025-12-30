package xyz.cottageindustries.cottfur.client.customization

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.minecraft.client.MinecraftClient
import xyz.cottageindustries.cottfur.CottfurConstants
import java.io.File
import java.io.FileReader
import java.nio.file.Files
import java.nio.file.Path

/**
 * Importer for Blockbench .bbmodel files.
 * Converts Blockbench models to GeckoLib-compatible format for use in-game.
 */
object BlockbenchImporter {
    
    // Directory for storing imported custom models
    private val CUSTOM_MODEL_DIR: Path by lazy {
        val configDir = MinecraftClient.getInstance().runDirectory.toPath()
            .resolve("config")
            .resolve("cottfur")
            .resolve("models")
        Files.createDirectories(configDir)
        configDir
    }
    
    private val gson = Gson()
    
    /**
     * Result of a model import operation.
     */
    sealed class ImportResult {
        data class Success(
            val modelId: String,
            val modelName: String,
            val boneCount: Int
        ) : ImportResult()
        
        data class Error(val message: String) : ImportResult()
    }
    
    /**
     * Blockbench model format version we support.
     */
    private const val SUPPORTED_FORMAT = "bedrock"
    
    /**
     * Import a Blockbench .bbmodel file.
     * 
     * @param file The .bbmodel file to import
     * @return ImportResult indicating success or failure
     */
    fun importModel(file: File): ImportResult {
        // Validate file
        if (!file.exists() || !file.canRead()) {
            return ImportResult.Error("File does not exist or cannot be read")
        }
        
        if (!file.name.lowercase().endsWith(".bbmodel")) {
            return ImportResult.Error("Only .bbmodel files are supported")
        }
        
        try {
            // Parse the JSON
            val json = FileReader(file).use { reader ->
                JsonParser.parseReader(reader).asJsonObject
            }
            
            // Validate format
            val format = json.get("meta")?.asJsonObject?.get("format_version")?.asString
            if (format == null) {
                return ImportResult.Error("Invalid Blockbench file: missing format version")
            }
            
            // Get model name
            val modelName = json.get("name")?.asString 
                ?: file.nameWithoutExtension
            
            // Validate it has geometry
            val elements = json.getAsJsonArray("elements")
            if (elements == null || elements.size() == 0) {
                return ImportResult.Error("Model has no elements")
            }
            
            // Validate it has required bones for player model
            val outliner = json.getAsJsonArray("outliner")
            val validationResult = validateBoneStructure(outliner)
            if (validationResult != null) {
                return ImportResult.Error(validationResult)
            }
            
            // Generate a unique model ID
            val modelId = generateModelId(modelName)
            
            // Convert to GeckoLib format
            val geoJson = convertToGeoFormat(json, modelId)
            
            // Save the converted model
            val outputFile = CUSTOM_MODEL_DIR.resolve("$modelId.geo.json").toFile()
            outputFile.writeText(gson.toJson(geoJson))
            
            // Also save the original for reference
            val originalFile = CUSTOM_MODEL_DIR.resolve("$modelId.bbmodel").toFile()
            file.copyTo(originalFile, overwrite = true)
            
            // Count bones
            val boneCount = countBones(outliner)
            
            CottfurConstants.LOGGER.info("Imported model: $modelName (ID: $modelId, $boneCount bones)")
            
            return ImportResult.Success(modelId, modelName, boneCount)
            
        } catch (e: Exception) {
            CottfurConstants.LOGGER.error("Failed to import model: ${e.message}", e)
            return ImportResult.Error("Failed to parse model: ${e.message}")
        }
    }
    
    /**
     * Validate that the model has the required bone structure for an anthro player model.
     * Returns null if valid, or an error message if invalid.
     */
    private fun validateBoneStructure(outliner: com.google.gson.JsonArray?): String? {
        if (outliner == null || outliner.size() == 0) {
            return "Model has no bone structure"
        }
        
        // Required bones for player model
        val requiredBones = setOf("head", "body", "left_arm", "right_arm", "left_leg", "right_leg")
        val foundBones = mutableSetOf<String>()
        
        // Recursively find all bone names
        fun findBones(element: com.google.gson.JsonElement) {
            when {
                element.isJsonObject -> {
                    val obj = element.asJsonObject
                    obj.get("name")?.asString?.let { foundBones.add(it.lowercase()) }
                    obj.get("children")?.asJsonArray?.forEach { findBones(it) }
                }
                element.isJsonArray -> {
                    element.asJsonArray.forEach { findBones(it) }
                }
            }
        }
        
        findBones(outliner)
        
        val missingBones = requiredBones - foundBones
        return if (missingBones.isNotEmpty()) {
            "Missing required bones: ${missingBones.joinToString(", ")}"
        } else {
            null
        }
    }
    
    /**
     * Convert Blockbench JSON format to GeckoLib geo.json format.
     */
    private fun convertToGeoFormat(bbmodel: JsonObject, modelId: String): JsonObject {
        val geoJson = JsonObject()
        
        // Set format version
        geoJson.addProperty("format_version", "1.12.0")
        
        // Create geometry array
        val geometryArray = com.google.gson.JsonArray()
        val geometry = JsonObject()
        
        // Description
        val description = JsonObject()
        description.addProperty("identifier", "geometry.cottfur.custom.$modelId")
        description.addProperty("texture_width", bbmodel.get("resolution")?.asJsonObject?.get("width")?.asInt ?: 64)
        description.addProperty("texture_height", bbmodel.get("resolution")?.asJsonObject?.get("height")?.asInt ?: 64)
        description.addProperty("visible_bounds_width", 2)
        description.addProperty("visible_bounds_height", 3)
        
        val boundsOffset = com.google.gson.JsonArray()
        boundsOffset.add(0)
        boundsOffset.add(1.5)
        boundsOffset.add(0)
        description.add("visible_bounds_offset", boundsOffset)
        
        geometry.add("description", description)
        
        // Convert bones
        val bones = convertBones(bbmodel)
        geometry.add("bones", bones)
        
        geometryArray.add(geometry)
        geoJson.add("minecraft:geometry", geometryArray)
        
        return geoJson
    }
    
    /**
     * Convert Blockbench outliner/elements to GeckoLib bones format.
     */
    private fun convertBones(bbmodel: JsonObject): com.google.gson.JsonArray {
        val bones = com.google.gson.JsonArray()
        
        val outliner = bbmodel.getAsJsonArray("outliner") ?: return bones
        val elements = bbmodel.getAsJsonArray("elements") ?: return bones
        
        // Create element lookup map by UUID
        val elementMap = mutableMapOf<String, JsonObject>()
        elements.forEach { element ->
            val obj = element.asJsonObject
            val uuid = obj.get("uuid")?.asString
            if (uuid != null) {
                elementMap[uuid] = obj
            }
        }
        
        // Process outliner recursively to build bone hierarchy
        fun processBone(boneElement: com.google.gson.JsonElement, parentName: String?): JsonObject? {
            if (!boneElement.isJsonObject) {
                // It's a UUID reference to an element, skip for bone creation
                return null
            }
            
            val boneObj = boneElement.asJsonObject
            val bone = JsonObject()
            
            val name = boneObj.get("name")?.asString ?: return null
            bone.addProperty("name", name)
            
            if (parentName != null) {
                bone.addProperty("parent", parentName)
            }
            
            // Pivot point
            val origin = boneObj.getAsJsonArray("origin")
            if (origin != null) {
                val pivot = com.google.gson.JsonArray()
                pivot.add(origin[0])
                pivot.add(origin[1])
                pivot.add(origin[2])
                bone.add("pivot", pivot)
            } else {
                val pivot = com.google.gson.JsonArray()
                pivot.add(0)
                pivot.add(0)
                pivot.add(0)
                bone.add("pivot", pivot)
            }
            
            // Get cubes from children that are element UUIDs
            val cubes = com.google.gson.JsonArray()
            val children = boneObj.getAsJsonArray("children")
            children?.forEach { child ->
                if (child.isJsonPrimitive) {
                    // This is a UUID reference to an element
                    val uuid = child.asString
                    val element = elementMap[uuid]
                    if (element != null) {
                        val cube = convertElementToCube(element)
                        if (cube != null) {
                            cubes.add(cube)
                        }
                    }
                }
            }
            
            if (cubes.size() > 0) {
                bone.add("cubes", cubes)
            }
            
            bones.add(bone)
            
            // Process child bones
            children?.forEach { child ->
                if (child.isJsonObject) {
                    processBone(child, name)
                }
            }
            
            return bone
        }
        
        // Process top-level bones
        outliner.forEach { topLevel ->
            processBone(topLevel, null)
        }
        
        return bones
    }
    
    /**
     * Convert a Blockbench element to a GeckoLib cube.
     */
    private fun convertElementToCube(element: JsonObject): JsonObject? {
        val cube = JsonObject()
        
        // Origin (from position)
        val from = element.getAsJsonArray("from") ?: return null
        val origin = com.google.gson.JsonArray()
        origin.add(from[0])
        origin.add(from[1])
        origin.add(from[2])
        cube.add("origin", origin)
        
        // Size (from position difference)
        val to = element.getAsJsonArray("to") ?: return null
        val size = com.google.gson.JsonArray()
        size.add(to[0].asFloat - from[0].asFloat)
        size.add(to[1].asFloat - from[1].asFloat)
        size.add(to[2].asFloat - from[2].asFloat)
        cube.add("size", size)
        
        // UV (simplified - just use position-based UV)
        val uv = element.getAsJsonObject("faces")?.getAsJsonObject("north")?.getAsJsonArray("uv")
        if (uv != null && uv.size() >= 2) {
            val uvArray = com.google.gson.JsonArray()
            uvArray.add(uv[0])
            uvArray.add(uv[1])
            cube.add("uv", uvArray)
        }
        
        return cube
    }
    
    /**
     * Count the number of bones in the outliner.
     */
    private fun countBones(outliner: com.google.gson.JsonArray?): Int {
        if (outliner == null) return 0
        
        var count = 0
        fun countRecursive(element: com.google.gson.JsonElement) {
            if (element.isJsonObject) {
                count++
                element.asJsonObject.getAsJsonArray("children")?.forEach { countRecursive(it) }
            } else if (element.isJsonArray) {
                element.asJsonArray.forEach { countRecursive(it) }
            }
        }
        
        outliner.forEach { countRecursive(it) }
        return count
    }
    
    /**
     * Generate a unique model ID from the model name.
     */
    private fun generateModelId(name: String): String {
        val baseName = name.lowercase()
            .replace(Regex("[^a-z0-9]"), "_")
            .take(20)
        val timestamp = System.currentTimeMillis() % 10000
        return "${baseName}_$timestamp"
    }
    
    /**
     * List all imported custom models.
     */
    fun listImportedModels(): List<String> {
        return CUSTOM_MODEL_DIR.toFile()
            .listFiles { file -> file.extension.lowercase() == "geo" }
            ?.map { it.nameWithoutExtension.removeSuffix(".geo") }
            ?: emptyList()
    }
    
    /**
     * Delete an imported model.
     */
    fun deleteModel(modelId: String): Boolean {
        val geoFile = CUSTOM_MODEL_DIR.resolve("$modelId.geo.json").toFile()
        val bbmodelFile = CUSTOM_MODEL_DIR.resolve("$modelId.bbmodel").toFile()
        
        var deleted = false
        if (geoFile.exists()) {
            geoFile.delete()
            deleted = true
        }
        if (bbmodelFile.exists()) {
            bbmodelFile.delete()
            deleted = true
        }
        
        return deleted
    }
}

