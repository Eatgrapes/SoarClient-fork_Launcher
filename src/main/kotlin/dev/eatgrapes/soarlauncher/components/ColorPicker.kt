package dev.eatgrapes.soarlauncher.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.eatgrapes.soarlauncher.color.ColorManager
import dev.eatgrapes.soarlauncher.i18n.i18n

@Composable
fun ColorPicker(
    colorManager: ColorManager,
    modifier: Modifier = Modifier
) {
    val hct = colorManager.getPrimaryHct()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        ColorSlider(
            label = i18n.text("ui.color.hue"),
            value = hct.hue.toFloat(),
            valueRange = 0f..360f,
            onValueChange = { colorManager.setHue(it) },
            colors = listOf(
                Color.Red, Color.Yellow, Color.Green,
                Color.Cyan, Color.Blue, Color.Magenta, Color.Red
            )
        )


        ColorPreview(colorManager = colorManager)
    }
}

@Composable
private fun ColorSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${value.toInt()}°",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
        ) {

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
            ) {
                val gradient = Brush.horizontalGradient(colors)
                drawRect(gradient)
            }


            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = valueRange,
                modifier = Modifier.fillMaxSize(),
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.Transparent,
                    inactiveTrackColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
private fun ColorPreview(
    colorManager: ColorManager,
    modifier: Modifier = Modifier
) {
    val palette = colorManager.palette

    if (palette != null) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = i18n.text("ui.color.preview"),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ColorSwatch(
                    color = palette.getPrimary(),
                    label = i18n.text("ui.color.primary"),
                    modifier = Modifier.weight(1f)
                )
                ColorSwatch(
                    color = palette.getSecondary(),
                    label = i18n.text("ui.color.secondary"),
                    modifier = Modifier.weight(1f)
                )
                ColorSwatch(
                    color = palette.getTertiary(),
                    label = i18n.text("ui.color.tertiary"),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ColorSwatch(
    color: Color,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(color, RoundedCornerShape(8.dp))
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}