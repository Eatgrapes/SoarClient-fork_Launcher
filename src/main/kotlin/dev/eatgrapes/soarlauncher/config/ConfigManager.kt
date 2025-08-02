package dev.eatgrapes.soarlauncher.config

import java.io.File
import java.util.Properties

object ConfigManager {
    private const val CONFIG_FILE = "config.properties"
    private val configFile = File(System.getProperty("user.dir"), CONFIG_FILE)
    private val properties = Properties()
    
    private const val KEY_DARK_MODE = "ui.dark_mode"
    private const val KEY_SELECTED_COLOR = "ui.selected_color"
    
    init {
        loadConfig()
    }
    
    private fun loadConfig() {
        if (configFile.exists()) {
            configFile.inputStream().use { properties.load(it) }
        }
    }
    
    private fun saveConfig() {
        configFile.outputStream().use { properties.store(it, "SoarClient Configuration") }
    }
    
    fun isDarkModeEnabled(): Boolean {
        return properties.getProperty(KEY_DARK_MODE, "false").toBoolean()
    }
    
    fun setDarkMode(enabled: Boolean) {
        properties.setProperty(KEY_DARK_MODE, enabled.toString())
        saveConfig()
    }
    
    fun getSelectedColor(): String {
        return properties.getProperty(KEY_SELECTED_COLOR, "220.0,36.0,60.0")
    }
    
    fun setSelectedColor(color: String) {
        properties.setProperty(KEY_SELECTED_COLOR, color)
        saveConfig()
    }
}