package dev.eatgrapes.soarlauncher.color

import androidx.compose.runtime.*
import dev.eatgrapes.soarlauncher.config.ConfigManager

class ColorManager {
    private var _palette by mutableStateOf<ColorPalette?>(null)
    private var _isDarkMode by mutableStateOf(ConfigManager.isDarkModeEnabled())
    private var _primaryHct by mutableStateOf(parseHctFromString(ConfigManager.getSelectedColor()))

    val palette: ColorPalette?
        get() = _palette

    init {
        updatePalette()
    }

    fun setDarkMode(isDark: Boolean) {
        if (_isDarkMode != isDark) {
            _isDarkMode = isDark
            ConfigManager.setDarkMode(isDark)
            updatePalette()
        }
    }

    fun setPrimaryColor(hct: Hct) {
        if (_primaryHct != hct) {
            _primaryHct = hct
            ConfigManager.setSelectedColor(hctToString(hct))
            updatePalette()
        }
    }

    fun setHue(hue: Float) {
        val newHct = Hct.from(
            hue = hue.toDouble(),
            chroma = 36.0,
            tone = 60.0
        )
        setPrimaryColor(newHct)
    }

    private fun parseHctFromString(hctString: String): Hct {
        val parts = hctString.split(",")
        if (parts.size == 3) {
            try {
                val hue = parts[0].toDouble()
                val chroma = parts[1].toDouble()
                val tone = parts[2].toDouble()
                return Hct.from(hue, chroma, tone)
            } catch (e: NumberFormatException) {
                return Hct.from(220.0, 36.0, 60.0)
            }
        }
        return Hct.from(220.0, 36.0, 60.0)
    }

    private fun hctToString(hct: Hct): String {
        return "${hct.hue},${hct.chroma},${hct.tone}"
    }

    private fun updatePalette() {
        _palette = ColorPalette(_primaryHct, _isDarkMode)
    }

    fun isDarkMode(): Boolean = _isDarkMode
    fun getPrimaryHct(): Hct = _primaryHct
}