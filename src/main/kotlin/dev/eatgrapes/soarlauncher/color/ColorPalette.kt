package dev.eatgrapes.soarlauncher.color

import androidx.compose.ui.graphics.Color

class ColorPalette(
    private val hct: Hct,
    private val isDark: Boolean
) {

    fun getPrimary(): Color {
        val tone = if (isDark) 80.0 else 40.0
        return hctToColor(hct.copy(tone = tone))
    }

    fun getOnPrimary(): Color {
        val tone = if (isDark) 20.0 else 100.0
        return hctToColor(hct.copy(tone = tone))
    }

    fun getPrimaryContainer(): Color {
        val tone = if (isDark) 30.0 else 90.0
        return hctToColor(hct.copy(tone = tone))
    }

    fun getOnPrimaryContainer(): Color {
        val tone = if (isDark) 90.0 else 10.0
        return hctToColor(hct.copy(tone = tone))
    }

    fun getSecondary(): Color {
        val secondaryHct = hct.copy(hue = (hct.hue + 60) % 360)
        val tone = if (isDark) 80.0 else 40.0
        return hctToColor(secondaryHct.copy(tone = tone))
    }

    fun getSecondaryContainer(): Color {
        val secondaryHct = hct.copy(hue = (hct.hue + 60) % 360)
        val tone = if (isDark) 30.0 else 90.0
        return hctToColor(secondaryHct.copy(tone = tone))
    }

    fun getTertiary(): Color {
        val tertiaryHct = hct.copy(hue = (hct.hue + 120) % 360)
        val tone = if (isDark) 80.0 else 40.0
        return hctToColor(tertiaryHct.copy(tone = tone))
    }

    fun getSurface(): Color {
        val tone = if (isDark) 10.0 else 99.0
        return hctToColor(Hct(0.0, 0.0, tone))
    }

    fun getOnSurface(): Color {
        val tone = if (isDark) 90.0 else 10.0
        return hctToColor(Hct(0.0, 0.0, tone))
    }

    fun getSurfaceVariant(): Color {
        val tone = if (isDark) 30.0 else 90.0
        return hctToColor(Hct(hct.hue, hct.chroma * 0.3, tone))
    }

    fun getOnSurfaceVariant(): Color {
        val tone = if (isDark) 80.0 else 30.0
        return hctToColor(Hct(hct.hue, hct.chroma * 0.3, tone))
    }

    // 添加背景相关颜色方法
    fun getBackground(): Color {
        val tone = if (isDark) 6.0 else 98.0
        return hctToColor(Hct(0.0, 0.0, tone))
    }

    fun getSurfaceContainer(): Color {
        val tone = if (isDark) 12.0 else 94.0
        return hctToColor(Hct(hct.hue, hct.chroma * 0.2, tone))
    }

    fun getSurfaceContainerHigh(): Color {
        val tone = if (isDark) 17.0 else 92.0
        return hctToColor(Hct(hct.hue, hct.chroma * 0.2, tone))
    }

    private fun hctToColor(hct: Hct): Color {
        val argb = hct.toArgb()
        return Color(argb)
    }

    fun isDarkMode(): Boolean = isDark
    fun getHct(): Hct = hct
}