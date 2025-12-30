package xyz.cottageindustries.cottfur.client.ui

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.text.Text
import xyz.cottageindustries.cottfur.CottfurConstants
import xyz.cottageindustries.cottfur.client.model.AnthroModelType
import xyz.cottageindustries.cottfur.client.network.CottfurClientNetworking
import xyz.cottageindustries.cottfur.data.PlayerModelConfig
import xyz.cottageindustries.cottfur.data.PlayerModelDataManager

/**
 * Main customization screen for selecting and configuring anthro models.
 * Accessed via a button in the inventory screen or keybind.
 */
class AnthroCustomizationScreen(private val parent: Screen?) : Screen(Text.translatable("gui.cottfur.customization.title")) {
    
    // Current configuration being edited
    private var currentConfig: PlayerModelConfig = PlayerModelConfig.DEFAULT
    private var selectedModelType: AnthroModelType = AnthroModelType.NONE
    
    // UI state
    private var currentTab = Tab.MODEL_SELECT
    
    // Color values
    private var primaryColor = 0xFFFFFF
    private var secondaryColor = 0x888888
    private var accentColor = 0xFF0000
    
    // UI elements
    private val modelButtons = mutableListOf<ButtonWidget>()
    private var applyButton: ButtonWidget? = null
    private var cancelButton: ButtonWidget? = null
    
    enum class Tab {
        MODEL_SELECT,
        COLORS,
        PATTERNS,
        IMPORT
    }
    
    override fun init() {
        super.init()
        
        // Load current player config
        val player = MinecraftClient.getInstance().player
        player?.let {
            currentConfig = PlayerModelDataManager.getConfig(it.uuid)
            selectedModelType = AnthroModelType.fromId(currentConfig.modelTypeId)
            primaryColor = currentConfig.primaryColor
            secondaryColor = currentConfig.secondaryColor
            accentColor = currentConfig.accentColor
        }
        
        val centerX = width / 2
        val startY = 50
        
        // Tab buttons at top
        val tabWidth = 80
        val tabSpacing = 5
        val totalTabWidth = (tabWidth * 4) + (tabSpacing * 3)
        val tabStartX = centerX - (totalTabWidth / 2)
        
        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.cottfur.tab.models")) { 
            currentTab = Tab.MODEL_SELECT
            clearAndInit()
        }.dimensions(tabStartX, 20, tabWidth, 20).build())
        
        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.cottfur.tab.colors")) { 
            currentTab = Tab.COLORS
            clearAndInit()
        }.dimensions(tabStartX + tabWidth + tabSpacing, 20, tabWidth, 20).build())
        
        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.cottfur.tab.patterns")) { 
            currentTab = Tab.PATTERNS
            clearAndInit()
        }.dimensions(tabStartX + (tabWidth + tabSpacing) * 2, 20, tabWidth, 20).build())
        
        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.cottfur.tab.import")) { 
            currentTab = Tab.IMPORT
            clearAndInit()
        }.dimensions(tabStartX + (tabWidth + tabSpacing) * 3, 20, tabWidth, 20).build())
        
        // Content based on current tab
        when (currentTab) {
            Tab.MODEL_SELECT -> initModelSelectTab(centerX, startY)
            Tab.COLORS -> initColorsTab(centerX, startY)
            Tab.PATTERNS -> initPatternsTab(centerX, startY)
            Tab.IMPORT -> initImportTab(centerX, startY)
        }
        
        // Bottom buttons - Apply and Cancel
        val buttonY = height - 30
        
        applyButton = addDrawableChild(
            ButtonWidget.builder(Text.translatable("gui.cottfur.apply")) { applyChanges() }
                .dimensions(centerX - 105, buttonY, 100, 20)
                .build()
        )
        
        cancelButton = addDrawableChild(
            ButtonWidget.builder(Text.translatable("gui.done")) { close() }
                .dimensions(centerX + 5, buttonY, 100, 20)
                .build()
        )
    }
    
    private fun initModelSelectTab(centerX: Int, startY: Int) {
        modelButtons.clear()
        
        val buttonWidth = 150
        val buttonHeight = 25
        val buttonSpacing = 5
        
        // Create buttons for each model type
        AnthroModelType.entries.forEachIndexed { index, modelType ->
            val x = centerX - buttonWidth / 2
            val y = startY + (buttonHeight + buttonSpacing) * index
            
            val button = ButtonWidget.builder(Text.literal(modelType.displayName)) {
                selectedModelType = modelType
                updateModelButtons()
            }.dimensions(x, y, buttonWidth, buttonHeight).build()
            
            modelButtons.add(button)
            addDrawableChild(button)
        }
        
        updateModelButtons()
    }
    
    private fun initColorsTab(centerX: Int, startY: Int) {
        val labelWidth = 100
        val inputWidth = 80
        val rowHeight = 30
        
        // Primary Color
        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.cottfur.color.primary")) {}
            .dimensions(centerX - 100, startY, labelWidth, 20).build())
        
        // Secondary Color
        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.cottfur.color.secondary")) {}
            .dimensions(centerX - 100, startY + rowHeight, labelWidth, 20).build())
        
        // Accent Color
        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.cottfur.color.accent")) {}
            .dimensions(centerX - 100, startY + rowHeight * 2, labelWidth, 20).build())
        
        // Color preview squares will be rendered in the render method
        
        // Preset colors
        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.cottfur.presets")) {}
            .dimensions(centerX - 75, startY + rowHeight * 4, 150, 20).build())
    }
    
    private fun initPatternsTab(centerX: Int, startY: Int) {
        // Pattern selection buttons
        val patterns = listOf("None", "Stripes", "Spots", "Gradient", "Two-Tone")
        val buttonWidth = 100
        val buttonHeight = 25
        val buttonSpacing = 5
        
        patterns.forEachIndexed { index, pattern ->
            val row = index / 3
            val col = index % 3
            val x = centerX - 160 + (col * (buttonWidth + buttonSpacing))
            val y = startY + (row * (buttonHeight + buttonSpacing))
            
            addDrawableChild(
                ButtonWidget.builder(Text.literal(pattern)) {
                    // Set pattern
                    CottfurConstants.LOGGER.debug("Selected pattern: $pattern")
                }.dimensions(x, y, buttonWidth, buttonHeight).build()
            )
        }
    }
    
    private fun initImportTab(centerX: Int, startY: Int) {
        addDrawableChild(
            ButtonWidget.builder(Text.translatable("gui.cottfur.import.texture")) {
                // Open file picker for texture
                CottfurConstants.LOGGER.info("Opening texture import dialog...")
            }.dimensions(centerX - 75, startY, 150, 20).build()
        )
        
        addDrawableChild(
            ButtonWidget.builder(Text.translatable("gui.cottfur.import.model")) {
                // Open file picker for model
                CottfurConstants.LOGGER.info("Opening model import dialog...")
            }.dimensions(centerX - 75, startY + 30, 150, 20).build()
        )
        
        // Info text about supported formats
    }
    
    private fun updateModelButtons() {
        modelButtons.forEachIndexed { index, button ->
            val modelType = AnthroModelType.entries[index]
            val isSelected = modelType == selectedModelType
            // Update button appearance to show selection
            button.message = Text.literal(
                if (isSelected) "▶ ${modelType.displayName}" else "  ${modelType.displayName}"
            )
        }
    }
    
    private fun applyChanges() {
        val player = MinecraftClient.getInstance().player ?: return
        
        val newConfig = PlayerModelConfig(
            modelTypeId = selectedModelType.modelId,
            customTextureId = currentConfig.customTextureId,
            primaryColor = primaryColor,
            secondaryColor = secondaryColor,
            accentColor = accentColor,
            patternId = currentConfig.patternId
        )
        
        // Update local storage
        PlayerModelDataManager.setConfig(player.uuid, newConfig)
        
        // Send to server
        CottfurClientNetworking.sendModelUpdate(newConfig)
        
        CottfurConstants.LOGGER.info("Applied model changes: ${selectedModelType.displayName}")
        
        close()
    }
    
    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
        
        // Draw title
        context.drawCenteredTextWithShadow(
            textRenderer,
            title,
            width / 2,
            8,
            0xFFFFFF
        )
        
        // Draw color preview squares if on colors tab
        if (currentTab == Tab.COLORS) {
            val centerX = width / 2
            val startY = 50
            val rowHeight = 30
            val squareSize = 20
            
            // Primary color square
            context.fill(centerX + 20, startY, centerX + 20 + squareSize, startY + squareSize, 0xFF000000.toInt() or primaryColor)
            
            // Secondary color square
            context.fill(centerX + 20, startY + rowHeight, centerX + 20 + squareSize, startY + rowHeight + squareSize, 0xFF000000.toInt() or secondaryColor)
            
            // Accent color square
            context.fill(centerX + 20, startY + rowHeight * 2, centerX + 20 + squareSize, startY + rowHeight * 2 + squareSize, 0xFF000000.toInt() or accentColor)
        }
        
        // Draw model preview on the right side
        // TODO: Implement 3D model preview using GeckoLib renderer
    }
    
    override fun close() {
        client?.setScreen(parent)
    }
    
    companion object {
        /**
         * Open the customization screen
         */
        fun open() {
            val client = MinecraftClient.getInstance()
            client.setScreen(AnthroCustomizationScreen(client.currentScreen))
        }
    }
}

