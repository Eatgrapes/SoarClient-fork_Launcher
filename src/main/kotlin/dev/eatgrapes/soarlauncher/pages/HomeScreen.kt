package dev.eatgrapes.soarlauncher.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import dev.eatgrapes.soarlauncher.i18n.i18n
import dev.eatgrapes.soarlauncher.launch.GameLauncher
import dev.eatgrapes.soarlauncher.utils.ResourceManager
import dev.eatgrapes.soarlauncher.utils.ResourceManager.DownloadState
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToMods: () -> Unit,
    onStartGame: () -> Unit
) {
    var downloadProgress by remember { mutableStateOf(ResourceManager.currentProgress) }
    var isProcessing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var isLaunching by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }

    val updateDialogMessage = { message: String ->
        dialogMessage = message
        showDialog = true
    }
    
    fun launchGame() {
        coroutineScope.launch {
            isLaunching = true
            
            try {
                if (!ResourceManager.isJava21Installed()) {
                    updateDialogMessage(i18n.text("ui.home.java_not_found"))
                    isLaunching = false
                    return@launch
                }
                
                if (!ResourceManager.areMinecraftFilesDownloaded()) {
                    updateDialogMessage(i18n.text("ui.home.minecraft_files_not_found"))
                    isLaunching = false
                    return@launch
                }
                
                val modsDir = java.nio.file.Paths.get(ResourceManager.ASSETS_FOLDER, "versions", "mods").toFile()
                val fabricApiExists = modsDir.listFiles()?.any { it.name.startsWith("fabric-api") } ?: false
                if (!fabricApiExists) {
                    updateDialogMessage(i18n.text("ui.home.fabric_api_not_found"))
                    var downloadCompleted = false
                    var downloadError = false
                    
                    ResourceManager.downloadFabricApi { progress ->
                        when (progress.state) {
                            ResourceManager.DownloadState.DOWNLOADING -> {
                                updateDialogMessage(progress.message)
                            }
                            ResourceManager.DownloadState.COMPLETE -> {
                                updateDialogMessage(i18n.text("ui.home.fabric_api_downloaded"))
                                downloadCompleted = true
                            }
                            ResourceManager.DownloadState.ERROR -> {
                                updateDialogMessage("${i18n.text("ui.home.fabric_api_download_failed")}: ${progress.message}")
                                downloadError = true
                            }
                            else -> {}
                        }
                    }
                    
                    while (!downloadCompleted && !downloadError) {
                        kotlinx.coroutines.delay(100)
                    }
                    
                    if (downloadError) {
                        isLaunching = false
                        return@launch
                    }
                }
                
                val success = GameLauncher.launchGame {
                    println("Game started successfully")
                }
                
                if (!success) {
                    updateDialogMessage(i18n.text("ui.home.launch_failed"))
                    isLaunching = false
                    return@launch
                }
                
                onStartGame()
            } catch (e: Exception) {
                updateDialogMessage("${i18n.text("ui.home.launch_failed")}: ${e.message}")
                isLaunching = false
            }
        }
    }

    LaunchedEffect(Unit) {
        if (ResourceManager.isJava21Installed() && ResourceManager.areMinecraftFilesDownloaded() && ResourceManager.areAdditionalAssetsExtracted()) {
            downloadProgress = downloadProgress.copy(state = DownloadState.COMPLETE, message = i18n.text("ui.home.ready"))
        } else {
            downloadProgress = downloadProgress.copy(state = DownloadState.IDLE, message = i18n.text("ui.home.launch"))
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(
            modifier = Modifier
                .padding(top = 30.dp)
        ) {

            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(200.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    )
            )
            

            Image(
                painter = painterResource("soar/logo.png"),
                contentDescription = "Logo",
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(170.dp)
            )
        }
        

        Text(
            text = "SoarClient",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 50.sp
            ),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 1.dp)
        )
        

        Text(
            text = i18n.text("ui.home.subtitle"),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Light,
            modifier = Modifier.padding(top = 5.dp)
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 32.dp)
                .height(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (GameLauncher.isGameCurrentlyRunning()) {
                        MaterialTheme.colorScheme.secondary
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
                .clickable(enabled = !isProcessing && !GameLauncher.isGameCurrentlyRunning()) {
                    if (isProcessing || isLaunching) return@clickable
                    
                    if (GameLauncher.isGameCurrentlyRunning()) {
                        updateDialogMessage("${i18n.text("ui.home.game_already_running")} (${GameLauncher.getGameProcessId()})")
                        return@clickable
                    }
                    
                    coroutineScope.launch {
                        isProcessing = true
                        
                        val modsDir = java.nio.file.Paths.get(ResourceManager.ASSETS_FOLDER, "versions", "mods").toFile()
                        val fabricApiExists = modsDir.listFiles()?.any { it.name.startsWith("fabric-api") } ?: false
                        if (!fabricApiExists) {
                            downloadProgress = downloadProgress.copy(
                                state = DownloadState.DOWNLOADING,
                                message = i18n.text("ui.home.downloading_fabric_api")
                            )
                            
                            var downloadCompleted = false
                            var downloadError = false
                            
                            ResourceManager.downloadFabricApi { downloadProgress = it }
                            ResourceManager.downloadFabricApi { progress ->
                                downloadProgress = progress
                                when (progress.state) {
                                    ResourceManager.DownloadState.DOWNLOADING -> {
                                    }
                                    ResourceManager.DownloadState.COMPLETE -> {
                                        downloadCompleted = true
                                    }
                                    ResourceManager.DownloadState.ERROR -> {
                                        downloadError = true
                                    }
                                    else -> {}
                                }
                            }
                            
                            while (!downloadCompleted && !downloadError) {
                                kotlinx.coroutines.delay(100)
                            }
                            
                            if (downloadError) {
                                isProcessing = false
                                return@launch
                            }
                        }

                        ResourceManager.checkAndDownloadVersionInfo { downloadProgress = it }
                        if (downloadProgress.state == DownloadState.ERROR) {
                            isProcessing = false
                            return@launch
                        }

                        ResourceManager.downloadDependencies { downloadProgress = it }
                        if (downloadProgress.state == DownloadState.ERROR) {
                            isProcessing = false
                            return@launch
                        }

                        ResourceManager.downloadAssetIndex { downloadProgress = it }
                        if (downloadProgress.state == DownloadState.ERROR) {
                            isProcessing = false
                            return@launch
                        }

                        ResourceManager.downloadAssets { downloadProgress = it }
                        if (downloadProgress.state == DownloadState.ERROR) {
                            isProcessing = false
                            return@launch
                        }

                        if (!ResourceManager.areAdditionalAssetsExtracted()) {
                            ResourceManager.downloadAndExtractAdditionalAssets { downloadProgress = it }
                            if (downloadProgress.state == DownloadState.ERROR) {
                                isProcessing = false
                                return@launch
                            }
                        }

                        if (!ResourceManager.isJava21Installed()) {
                            ResourceManager.downloadJava21 { downloadProgress = it }
                            if (downloadProgress.state == DownloadState.ERROR) {
                                isProcessing = false
                                return@launch
                            }
                        }

                        ResourceManager.downloadMinecraftFiles { downloadProgress = it }
                        if (downloadProgress.state == DownloadState.ERROR) {
                            isProcessing = false
                            return@launch
                        }

                        ResourceManager.downloadSoarClientJar { downloadProgress = it }
                        if (downloadProgress.state == DownloadState.ERROR) {
                            isProcessing = false
                            return@launch
                        }
                        ResourceManager.downloadFabricApi { downloadProgress = it }
                        if (downloadProgress.state == DownloadState.ERROR) {
                            isProcessing = false
                            return@launch
                        }

                        val allReady = ResourceManager.isJava21Installed() &&
                                       ResourceManager.areMinecraftFilesDownloaded() &&
                                       ResourceManager.areAdditionalAssetsExtracted() &&
                                       ResourceManager.areAssetsDownloaded() &&
                                       ResourceManager.isSoarClientJarDownloaded()

                        if (allReady) {
                            downloadProgress = downloadProgress.copy(state = DownloadState.COMPLETE, message = i18n.text("ui.home.ready"))
                        } else {
                            downloadProgress = downloadProgress.copy(state = DownloadState.ERROR, message = i18n.text("ui.home.resources_not_ready"))
                        }
                        
                        isProcessing = false
                        
                        launchGame()
                    }
                },
                contentAlignment = Alignment.Center
            ) {
                if (isProcessing && (downloadProgress.state == DownloadState.DOWNLOADING || downloadProgress.state == DownloadState.EXTRACTING)) {
                    LinearProgressIndicator(
                        progress = downloadProgress.progress,
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        trackColor = Color.Transparent
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    val textStyle = MaterialTheme.typography.titleMedium.copy(
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.75f),
                            offset = Offset(2f, 2f),
                            blurRadius = 4f
                        )
                    )

                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when {
                            GameLauncher.isGameCurrentlyRunning() -> "${i18n.text("ui.home.running")} (${GameLauncher.getGameProcessId()})"
                            isProcessing -> downloadProgress.message
                            downloadProgress.state == DownloadState.COMPLETE -> i18n.text("ui.home.ready")
                            else -> i18n.text("ui.home.launch")
                        },
                        style = textStyle
                    )
                }
            }
        }
        
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(i18n.text("ui.home.dialog_title")) },
                text = { Text(dialogMessage) },
                confirmButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text(i18n.text("ui.ok"))
                    }
                }
            )
        }
    }