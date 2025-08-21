package dev.eatgrapes.soarlauncher.config

import java.io.File
import java.util.Properties

object ConfigManager {
    private const val CONFIG_FILE = "config.properties"
    private val configFile = File(System.getProperty("user.dir"), CONFIG_FILE)
    private val properties = Properties()
    
    private const val KEY_DARK_MODE = "ui.dark_mode"
    private const val KEY_SELECTED_COLOR = "ui.selected_color"
    private const val KEY_RAM_ALLOCATION = "game.ram_allocation"
    private const val KEY_PLAYER_NAME = "game.player_name"
    
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

    fun getRamAllocation(): Int {
        return properties.getProperty(KEY_RAM_ALLOCATION, "4").toIntOrNull() ?: 4
    }

    fun setRamAllocation(gb: Int) {
        properties.setProperty(KEY_RAM_ALLOCATION, gb.toString())
        saveConfig()
    }

    fun getPlayerName(): String {
        return properties.getProperty(KEY_PLAYER_NAME, "Player")
    }

    fun setPlayerName(name: String) {
        properties.setProperty(KEY_PLAYER_NAME, name)
        saveConfig()
    }
}