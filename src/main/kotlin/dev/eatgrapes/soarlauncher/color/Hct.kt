package dev.eatgrapes.soarlauncher.color

import kotlin.math.*

data class Hct(
    val hue: Double,
    val chroma: Double,
    val tone: Double
) {
    companion object {
        fun from(hue: Double, chroma: Double, tone: Double): Hct {
            return Hct(
                hue.coerceIn(0.0, 360.0),
                chroma.coerceAtLeast(0.0),
                tone.coerceIn(0.0, 100.0)
            )
        }

        fun fromInt(argb: Int): Hct {
            val r = ((argb shr 16) and 0xFF) / 255.0
            val g = ((argb shr 8) and 0xFF) / 255.0
            val b = (argb and 0xFF) / 255.0

            // 简化的RGB到HCT转换
            val max = maxOf(r, g, b)
            val min = minOf(r, g, b)
            val delta = max - min

            val hue = when {
                delta == 0.0 -> 0.0
                max == r -> 60 * (((g - b) / delta) % 6)
                max == g -> 60 * (((b - r) / delta) + 2)
                else -> 60 * (((r - g) / delta) + 4)
            }.let { if (it < 0) it + 360 else it }

            val chroma = if (max == 0.0) 0.0 else delta / max * 100
            val tone = max * 100

            return Hct(hue, chroma, tone)
        }
    }

    fun toArgb(): Int {
        val normalizedTone = tone / 100.0
        val normalizedChroma = chroma / 100.0

        val c = normalizedChroma * normalizedTone
        val x = c * (1 - abs(((hue / 60.0) % 2) - 1))
        val m = normalizedTone - c

        val (r, g, b) = when ((hue / 60.0).toInt()) {
            0 -> Triple(c, x, 0.0)
            1 -> Triple(x, c, 0.0)
            2 -> Triple(0.0, c, x)
            3 -> Triple(0.0, x, c)
            4 -> Triple(x, 0.0, c)
            else -> Triple(c, 0.0, x)
        }

        val red = ((r + m) * 255).toInt().coerceIn(0, 255)
        val green = ((g + m) * 255).toInt().coerceIn(0, 255)
        val blue = ((b + m) * 255).toInt().coerceIn(0, 255)

        return (0xFF shl 24) or (red shl 16) or (green shl 8) or blue
    }
}