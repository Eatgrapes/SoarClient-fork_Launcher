package dev.eatgrapes.soarlauncher

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Minimize
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Person
import dev.eatgrapes.soarlauncher.i18n.*
import dev.eatgrapes.soarlauncher.pages.HomeScreen
import dev.eatgrapes.soarlauncher.pages.SettingsScreen
import dev.eatgrapes.soarlauncher.pages.ModScreen
import dev.eatgrapes.soarlauncher.pages.ProfileScreen
import dev.eatgrapes.soarlauncher.color.ColorManager
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import androidx.compose.ui.zIndex
import java.awt.Cursor
import java.awt.MouseInfo

//少羽牛逼

fun main() = application {
    val windowState = rememberWindowState(
        size = androidx.compose.ui.unit.DpSize(400.dp, 600.dp),
        position = WindowPosition.Aligned(Alignment.Center)
    )

    val isDarkTheme = isSystemInDarkTheme()
    val icon = if (isDarkTheme) {
        painterResource("soar/logo.png")
    } else {
        painterResource("soar/logo.dark.png")
    }

    Window(
        onCloseRequest = { kotlin.system.exitProcess(0) },
        title = "Soar-fork Launcher",
        state = windowState,
        undecorated = true,
        transparent = true,
        resizable = false,
        icon = icon
    ) {
        val colorManager = remember { ColorManager() }
        var selectedItem by remember { mutableStateOf("Home") }
        var restartKey by remember { mutableStateOf(0) }

        val defaultColorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()

        val customColorScheme = remember(colorManager.palette, isDarkTheme) {
            colorManager.palette?.let { palette ->
                if (colorManager.isDarkMode()) {
                    darkColorScheme(
                        primary = palette.getPrimary(),
                        onPrimary = palette.getOnPrimary(),
                        primaryContainer = palette.getPrimaryContainer(),
                        onPrimaryContainer = palette.getOnPrimaryContainer(),
                        secondary = palette.getSecondary(),
                        secondaryContainer = palette.getSecondaryContainer(),
                        surface = palette.getSurface(),
                        onSurface = palette.getOnSurface(),
                        surfaceVariant = palette.getSurfaceVariant(),
                        onSurfaceVariant = palette.getOnSurfaceVariant()
                    )
                } else {
                    lightColorScheme(
                        primary = palette.getPrimary(),
                        onPrimary = palette.getOnPrimary(),
                        primaryContainer = palette.getPrimaryContainer(),
                        onPrimaryContainer = palette.getOnPrimaryContainer(),
                        secondary = palette.getSecondary(),
                        secondaryContainer = palette.getSecondaryContainer(),
                        surface = palette.getSurface(),
                        onSurface = palette.getOnSurface(),
                        surfaceVariant = palette.getSurfaceVariant(),
                        onSurfaceVariant = palette.getOnSurfaceVariant()
                    )
                }
            } ?: defaultColorScheme
        }

        MaterialTheme(colorScheme = customColorScheme) {
            Surface(modifier = Modifier.fillMaxSize(), color = Color.Transparent) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(25.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .align(Alignment.TopStart)
                                .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR)))
                                .pointerInput(Unit) {
                                    awaitPointerEventScope {
                                        var startMouseX = 0
                                        var startMouseY = 0
                                        var startWinXdp = 0f
                                        var startWinYdp = 0f
                                        var lastUpdate = 0L
                                        val density = this@pointerInput.density

                                        while (true) {
                                            val event = awaitPointerEvent()
                                            when (event.type) {
                                                PointerEventType.Press -> {
                                                    MouseInfo.getPointerInfo()?.location?.let { p ->
                                                        startMouseX = p.x
                                                        startMouseY = p.y
                                                        startWinXdp = windowState.position.x.value.takeIf { it.isFinite() } ?: 0f
                                                        startWinYdp = windowState.position.y.value.takeIf { it.isFinite() } ?: 0f
                                                    }
                                                }
                                                PointerEventType.Move -> {
                                                    val change = event.changes.firstOrNull()
                                                    if (change != null && change.pressed) {
                                                        val now = System.nanoTime()
                                                        if (now - lastUpdate > 16_000_000) {
                                                            MouseInfo.getPointerInfo()?.location?.let { p ->
                                                                val deltaXdp = (p.x - startMouseX) / density
                                                                val deltaYdp = (p.y - startMouseY) / density
                                                                windowState.position = WindowPosition.Absolute(
                                                                    (startWinXdp + deltaXdp).dp,
                                                                    (startWinYdp + deltaYdp).dp
                                                                )
                                                            }
                                                            lastUpdate = now
                                                        }
                                                        change.consume()
                                                    }
                                                }
                                                PointerEventType.Release -> {}
                                            }
                                        }
                                    }
                                }
                        )

                        Row(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 12.dp, end = 12.dp)
                                .zIndex(1f),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { windowState.isMinimized = true }, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.Default.Minimize, contentDescription = i18n.text("ui.minimize"), tint = MaterialTheme.colorScheme.onSurface)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = { kotlin.system.exitProcess(0) }, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.Default.Close, contentDescription = i18n.text("ui.close"), tint = MaterialTheme.colorScheme.onSurface)
                            }
                        }

                        
                        var isDownloading by remember { mutableStateOf(false) }
                        var showDownloadWarning by remember { mutableStateOf(false) }
                        var targetPage by remember { mutableStateOf("Home") }

                        
                        LaunchedEffect(Unit) {
                            
                        }

                        
                        if (showDownloadWarning) {
                            AlertDialog(
                                onDismissRequest = { showDownloadWarning = false },
                                title = { Text(i18n.text("ui.download_warning_title")) },
                                text = { Text(i18n.text("ui.download_warning_message")) },
                                confirmButton = {
                                    TextButton(onClick = { showDownloadWarning = false }) {
                                        Text(i18n.text("ui.ok"))
                                    }
                                }
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 40.dp, bottom = 80.dp)
                                .background(MaterialTheme.colorScheme.surface),
                            contentAlignment = Alignment.TopStart
                        ) {
                            when (selectedItem) {
                                "Home" -> HomeScreen(
                                    onNavigateToSettings = { selectedItem = "Settings" },
                                    onNavigateToMods = { selectedItem = "Mods" },
                                    onStartGame = { selectedItem = "Profile" }
                                )
                                "Mods" -> ModScreen()
                                "Settings" -> SettingsScreen(onLanguageChanged = { restartKey++ }, colorManager = colorManager)
                                "Profile" -> ProfileScreen()
                            }
                        }
                        
                        NavigationBar(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .align(Alignment.BottomStart),
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ) {
                            NavigationBarItem(
                                selected = selectedItem == "Home",
                                onClick = { 
                                    if (isDownloading) {
                                        showDownloadWarning = true
                                    } else {
                                        selectedItem = "Home"
                                    }
                                },
                                icon = { Icon(Icons.Default.Home, contentDescription = i18n.text("ui.home")) },
                                label = { Text(i18n.text("ui.home")) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            NavigationBarItem(
                                selected = selectedItem == "Mods",
                                onClick = { 
                                    if (isDownloading) {
                                        showDownloadWarning = true
                                    } else {
                                        selectedItem = "Mods"
                                    }
                                },
                                icon = { Icon(Icons.Default.Inventory2, contentDescription = i18n.text("ui.mods")) },
                                label = { Text(i18n.text("ui.mods")) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            NavigationBarItem(
                                selected = selectedItem == "Settings",
                                onClick = { 
                                    if (isDownloading) {
                                        showDownloadWarning = true
                                    } else {
                                        selectedItem = "Settings"
                                    }
                                },
                                icon = { Icon(Icons.Default.Settings, contentDescription = i18n.text("ui.settings")) },
                                label = { Text(i18n.text("ui.settings")) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            NavigationBarItem(
                                selected = selectedItem == "Profile",
                                onClick = { 
                                    if (isDownloading) {
                                        showDownloadWarning = true
                                    } else {
                                        selectedItem = "Profile"
                                    }
                                },
                                icon = { Icon(Icons.Default.Person, contentDescription = i18n.text("ui.profile")) },
                                label = { Text(i18n.text("ui.profile")) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}