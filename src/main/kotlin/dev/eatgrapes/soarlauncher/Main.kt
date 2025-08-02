package dev.eatgrapes.soarlauncher

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Minimize
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import dev.eatgrapes.soarlauncher.i18n.*
import dev.eatgrapes.soarlauncher.pages.HomeScreen
import dev.eatgrapes.soarlauncher.pages.SettingsScreen
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
import kotlinx.coroutines.*

fun main() = application {
    val windowState = rememberWindowState(
        size = androidx.compose.ui.unit.DpSize(400.dp, 600.dp),
        position = WindowPosition.Aligned(Alignment.Center)
    )

    Window(
        onCloseRequest = { kotlin.system.exitProcess(0) },
        title = "Soar-fork Launcher",
        state = windowState,
        undecorated = true,
        transparent = true,
        resizable = false
    ) {
        val colorManager = remember { ColorManager() }
        var selectedItem by remember { mutableStateOf("Home") }
        var restartKey by remember { mutableStateOf(0) }

        val defaultColorScheme = MaterialTheme.colorScheme

        val customColorScheme = remember(colorManager.palette) {
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
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(25.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {

                            var isDragging by remember { mutableStateOf(false) }
                            var dragOffset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp)
                                    .align(Alignment.TopStart)
                                    .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR)))
                                    .pointerInput(Unit) {
                                        awaitPointerEventScope {
                                            while (true) {
                                                val event = awaitPointerEvent()
                                                when (event.type) {
                                                    androidx.compose.ui.input.pointer.PointerEventType.Press -> {
                                                        isDragging = true
                                                        dragOffset = event.changes.first().position
                                                    }

                                                    androidx.compose.ui.input.pointer.PointerEventType.Move -> {
                                                        if (isDragging && event.changes.isNotEmpty()) {
                                                            val change = event.changes.first()
                                                            if (change.pressed) {
                                                                val dragAmount = change.positionChange()
                                                                val currentX = windowState.position.x.value
                                                                val currentY = windowState.position.y.value
                                                                val safeX = if (currentX.isNaN()) 0f else currentX
                                                                val safeY = if (currentY.isNaN()) 0f else currentY
                                                                val newX = safeX + dragAmount.x
                                                                val newY = safeY + dragAmount.y
                                                                windowState.position =
                                                                    WindowPosition.Absolute(newX.dp, newY.dp)
                                                                change.consume()
                                                            }
                                                        }
                                                    }

                                                    androidx.compose.ui.input.pointer.PointerEventType.Release -> {
                                                        isDragging = false
                                                    }
                                                }
                                            }
                                        }
                                    }
                            )

    
                            Row(
                                modifier = Modifier.align(Alignment.TopEnd)
                                    .padding(top = 12.dp, end = 12.dp)
                                    .zIndex(1f),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { windowState.isMinimized = true },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Minimize,
                                        contentDescription = i18n.text("ui.minimize"),
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                IconButton(
                                    onClick = { kotlin.system.exitProcess(0) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = i18n.text("ui.close"),
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

    
                            Box(
                                modifier = Modifier.fillMaxSize()
                                    .padding(bottom = 80.dp)
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(16.dp),
                                contentAlignment = Alignment.TopStart
                            ) {
                                when (selectedItem) {
                                    "Home" -> HomeScreen()
                                    "Settings" -> SettingsScreen(
                                        onLanguageChanged = { restartKey++ },
                                        colorManager = colorManager
                                    )
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
                                    onClick = { selectedItem = "Home" },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.Home,
                                            contentDescription = i18n.text("ui.home")
                                        )
                                    },
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
                                    selected = selectedItem == "Settings",
                                    onClick = { selectedItem = "Settings" },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.Settings,
                                            contentDescription = i18n.text("ui.settings")
                                        )
                                    },
                                    label = { Text(i18n.text("ui.settings")) },
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