package dev.eatgrapes.soarlauncher.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.eatgrapes.soarlauncher.i18n.i18n
import dev.eatgrapes.soarlauncher.i18n.Language
import dev.eatgrapes.soarlauncher.i18n.TranslationManager
import dev.eatgrapes.soarlauncher.color.ColorManager
import dev.eatgrapes.soarlauncher.components.ColorPicker
import dev.eatgrapes.soarlauncher.config.ConfigManager
import com.sun.management.OperatingSystemMXBean
import java.lang.management.ManagementFactory

@Composable
fun SettingsScreen(
    onLanguageChanged: () -> Unit,
    colorManager: ColorManager
) {
    var selectedLanguage by remember { mutableStateOf(TranslationManager.getCurrentLanguage()) }
    var languageExpanded by remember { mutableStateOf(false) }
    var colorExpanded by remember { mutableStateOf(false) }
    var showRestartDialog by remember { mutableStateOf(false) }
    var pendingLanguage by remember { mutableStateOf<String?>(null) }
    
    val totalMemoryGB = try {
        val osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean::class.java)
        (osBean.totalPhysicalMemorySize / (1024 * 1024 * 1024)).coerceIn(1L, 32L)
    } catch (e: Exception) {
        16L
    }
    
    var ramAllocation by remember { 
        mutableStateOf(ConfigManager.getRamAllocation().coerceAtMost(totalMemoryGB.toInt())) 
    }

    val availableLanguages = remember { TranslationManager.getAvailableLanguages() }
    val scrollState = rememberScrollState()

    if (showRestartDialog) {
        AlertDialog(
            onDismissRequest = { showRestartDialog = false },
            title = { Text(i18n.text("ui.restart_title")) },
            text = { Text(i18n.text("ui.restart_message")) },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingLanguage?.let { langCode ->
                            TranslationManager.loadLanguage(langCode)
                        }
                        kotlin.system.exitProcess(0)
                    }
                ) {
                    Text(i18n.text("ui.restart_confirm"))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showRestartDialog = false
                        pendingLanguage = null
                    }
                ) {
                    Text(i18n.text("ui.restart_cancel"))
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = i18n.text("ui.settings"),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { languageExpanded = !languageExpanded },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = i18n.text("ui.language"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = if (languageExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                }

                AnimatedVisibility(
                    visible = languageExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        availableLanguages.forEach { languageCode ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedLanguage = languageCode
                                        pendingLanguage = languageCode
                                        showRestartDialog = true
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedLanguage == languageCode,
                                    onClick = {
                                        selectedLanguage = languageCode
                                        pendingLanguage = languageCode
                                        showRestartDialog = true
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = Language.getDisplayName(languageCode))
                            }
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = i18n.text("ui.theme.darkmode"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Switch(
                        checked = colorManager.isDarkMode(),
                        onCheckedChange = { colorManager.setDarkMode(it) }
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { colorExpanded = !colorExpanded },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = i18n.text("ui.color.settings"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = if (colorExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                }

                AnimatedVisibility(
                    visible = colorExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))
                        ColorPicker(colorManager = colorManager)
                    }
                }
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = i18n.text("ui.ram_allocation"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${ramAllocation}GB",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Slider(
                    value = ramAllocation.toFloat(),
                    onValueChange = { ramAllocation = it.toInt() },
                    valueRange = 1f..totalMemoryGB.toFloat(),
                    steps = (totalMemoryGB.toInt() - 2).coerceAtLeast(0),
                    onValueChangeFinished = {
                        ConfigManager.setRamAllocation(ramAllocation)
                    }
                )
            }
        }
    }
}