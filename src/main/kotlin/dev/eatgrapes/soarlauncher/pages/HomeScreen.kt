package dev.eatgrapes.soarlauncher.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.eatgrapes.soarlauncher.i18n.i18n
import dev.eatgrapes.soarlauncher.utils.ResourceManager
import dev.eatgrapes.soarlauncher.utils.ResourceManager.DownloadState

@Composable
fun HomeScreen() {
    var downloadProgress by remember { mutableStateOf(ResourceManager.currentProgress) }
    var isChecking by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        if (ResourceManager.isJava21Installed() && ResourceManager.areMinecraftFilesDownloaded()) {
            downloadProgress = downloadProgress.copy(state = DownloadState.COMPLETE, message = "准备就绪")
        }
    }
    
    var versionCheckComplete by remember { mutableStateOf(false) }
    
    LaunchedEffect(isChecking, versionCheckComplete) {
        if (isChecking) {
            if (!versionCheckComplete) {
                ResourceManager.checkAndDownloadVersionInfo { progress ->
                    downloadProgress = progress
                    if (progress.state == DownloadState.COMPLETE || progress.state == DownloadState.ERROR) {
                        versionCheckComplete = true
                    }
                }
            } else if (!ResourceManager.isJava21Installed()) {
                ResourceManager.downloadJava21 { progress ->
                    downloadProgress = progress
                    if (progress.state == DownloadState.COMPLETE || progress.state == DownloadState.ERROR) {
                        isChecking = false
                        versionCheckComplete = false
                    }
                }
            } else if (!ResourceManager.areMinecraftFilesDownloaded()) {
                ResourceManager.downloadMinecraftFiles { progress ->
                    downloadProgress = progress
                    if (progress.state == DownloadState.COMPLETE || progress.state == DownloadState.ERROR) {
                        isChecking = false
                        versionCheckComplete = false
                    }
                }
            } else {
                isChecking = false
                versionCheckComplete = false
            }
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
                    .size(220.dp)
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
        

        
        Button(
            onClick = { 
                if (downloadProgress.state == DownloadState.COMPLETE && !isChecking) {
                    // 启动逻辑
                } else if (!isChecking) {
                    // 每次点击启动时检查版本信息
                    versionCheckComplete = false
                    isChecking = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 32.dp)
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            enabled = !isChecking && downloadProgress.state != DownloadState.DOWNLOADING && downloadProgress.state != DownloadState.EXTRACTING
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (downloadProgress.state) {
                        DownloadState.CHECKING -> i18n.text("ui.home.checking")
                        DownloadState.DOWNLOADING -> i18n.text("ui.home.downloading")
                        DownloadState.EXTRACTING -> i18n.text("ui.home.extracting")
                        DownloadState.COMPLETE -> i18n.text("ui.home.ready")
                        else -> i18n.text("ui.home.launch")
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}