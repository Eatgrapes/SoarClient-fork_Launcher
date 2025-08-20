package dev.eatgrapes.soarlauncher.pages

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.eatgrapes.soarlauncher.i18n.i18n
import dev.eatgrapes.soarlauncher.utils.*
import kotlinx.coroutines.launch
import java.io.File
import java.net.HttpURLConnection
import java.net.URI
import java.nio.file.Paths
import javax.imageio.ImageIO
import java.io.BufferedInputStream
import java.io.FileOutputStream
import javax.swing.ImageIcon
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.platform.LocalDensity
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipFile
import org.jetbrains.skia.Image
import java.io.InputStream
import javax.imageio.spi.IIORegistry
import javax.imageio.spi.ImageReaderSpi

@Composable
fun ModScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var showSearch by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var onlineMods by remember { mutableStateOf<List<ModrinthProject>>(emptyList()) }
    var installedMods by remember { mutableStateOf<List<LocalModInfo>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val totalPages = 10

    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    LaunchedEffect(selectedTab) {
        coroutineScope.launch {
            val mods = getInstalledMods()
            installedMods = mods
        }

        if (selectedTab == 0) {
            coroutineScope.launch {
                isLoading = true
                errorMessage = null
                try {
                    val allMods = mutableListOf<ModrinthProject>()
                    for (i in 0 until totalPages) {
                        val result = ModrinthAPI.searchMods("", i * 20)
                        if (result != null) {
                            allMods.addAll(result.hits)
                        } else {
                            break
                        }
                    }
                    onlineMods = allMods
                } catch (e: Exception) {
                    errorMessage = e.message ?: "Unknown error"
                } finally {
                    isLoading = false
                }
            }
        }
    }

    fun loadOnlineMods(query: String) {
        coroutineScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val allMods = mutableListOf<ModrinthProject>()
                for (i in 0 until totalPages) {
                    val result = ModrinthAPI.searchMods(query, i * 20)
                    if (result != null) {
                        allMods.addAll(result.hits)
                    } else {
                        break
                    }
                }
                onlineMods = allMods
            } catch (e: Exception) {
                errorMessage = e.message ?: "Unknown error"
            } finally {
                isLoading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = i18n.text("ui.mods"),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.width(16.dp))

            if (selectedTab == 0) {
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text(i18n.text("ui.mods.search"), style = MaterialTheme.typography.labelSmall) },
                    modifier = Modifier
                        .weight(3f),
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                loadOnlineMods(searchQuery)
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = i18n.text("ui.mods.search"),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    },
                    textStyle = MaterialTheme.typography.labelSmall,
                    singleLine = true
                )
            }

            Spacer(Modifier.weight(1f))
        }

        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = {
                    selectedTab = 0
                },
                text = { Text(i18n.text("ui.mods.browse"), style = MaterialTheme.typography.labelMedium) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text(i18n.text("ui.mods.installed"), style = MaterialTheme.typography.labelMedium) }
            )
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                errorMessage != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${i18n.text("ui.mods.error")}: $errorMessage")
                    }
                }

                selectedTab == 0 -> {
                    OnlineModsList(onlineMods, installedMods)
                }

                selectedTab == 1 -> {
                    InstalledModsList(installedMods)
                }
            }
        }
    }
}

@Composable
fun OnlineModsList(mods: List<ModrinthProject>, installedMods: List<LocalModInfo>) {
    val scrollState = rememberScrollState()
    
    if (mods.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(i18n.text("ui.mods.no_mods_found"))
        }
        return
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(mods) { mod ->
            OnlineModItem(mod, installedMods)
        }
    }
}

@Composable
fun OnlineModItem(mod: ModrinthProject, installedMods: List<LocalModInfo>) {
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var modIcon by remember { mutableStateOf<ImageBitmap?>(null) }
    var isInstalled by remember { mutableStateOf(false) }
    
    // 加载在线图标
    LaunchedEffect(Unit) {
        if (mod.iconUrl != null && mod.iconUrl.isNotEmpty()) {
            loadOnlineImage(mod.iconUrl)?.let { modIcon = it }
        }
        
        // 检查是否已安装 - 改进的检测逻辑
        isInstalled = installedMods.any { installedMod -> 
            installedMod.name.equals(mod.title, ignoreCase = true) || 
            installedMod.id.equals(mod.slug, ignoreCase = true) ||
            installedMod.name.contains(mod.title, ignoreCase = true) ||
            mod.title.contains(installedMod.name, ignoreCase = true)
        }
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mod图标
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (modIcon != null) {
                    Image(
                        bitmap = modIcon!!,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (mod.iconUrl != null && mod.iconUrl.isNotEmpty()) {
                    Icon(Icons.Default.Android, contentDescription = null)
                } else {
                    Icon(Icons.Default.Extension, contentDescription = null)
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Mod信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = mod.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = mod.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
                
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${mod.downloads}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
    // Removed date and version display
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // 操作按钮
            Button(
                onClick = {
                    if (!isInstalled) {
                        coroutineScope.launch {
                            isLoading = true
                            try {
                                downloadMod(mod)
                                // 下载完成后更新状态
                                isInstalled = true
                            } catch (e: Exception) {
                                e.printStackTrace()
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                },
                enabled = !isInstalled && !isLoading,
                modifier = Modifier.height(36.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(if (isInstalled) i18n.text("ui.mods.installed") else i18n.text("ui.mods.install"), fontSize = MaterialTheme.typography.labelSmall.fontSize)
                }
            }
        }
    }
}

@Composable
fun InstalledModsList(mods: List<LocalModInfo>) {
    if (mods.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(i18n.text("ui.mods.no_installed_mods"))
        }
        return
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(mods) { mod ->
            InstalledModItem(mod)
        }
    }
}

@Composable
fun InstalledModItem(mod: LocalModInfo) {
    val coroutineScope = rememberCoroutineScope()
    var showUninstallDialog by remember { mutableStateOf(false) }
    var modIcon by remember { mutableStateOf<ImageBitmap?>(null) }
    
    // 加载本地图标
    LaunchedEffect(Unit) {
        loadImageFromJar(mod)?.let { modIcon = it }
    }
    
    // 不允许卸载SoarClient-fork和Fabric API
    val canUninstall = mod.id != "soarclient" && mod.id != "fabric-api"
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mod图标
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (modIcon != null) {
                    Image(
                        bitmap = modIcon!!,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (mod.icon != null) {
                    Icon(Icons.Default.Android, contentDescription = null)
                } else {
                    Icon(Icons.Default.Extension, contentDescription = null)
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Mod信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = mod.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = mod.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )
                
                if (mod.authors.isNotEmpty()) {
                    Text(
                        text = "${i18n.text("ui.mods.by")} ${mod.authors.joinToString(", ")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // 操作按钮
            if (canUninstall) {
                Button(
                    onClick = { showUninstallDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(i18n.text("ui.mods.uninstall"), fontSize = MaterialTheme.typography.labelSmall.fontSize)
                }
            } else {
                Button(
                    onClick = { },
                    enabled = false,
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(i18n.text("ui.mods.protected"), fontSize = MaterialTheme.typography.labelSmall.fontSize)
                }
            }
        }
    }
    
    // 卸载确认对话框
    if (showUninstallDialog) {
        AlertDialog(
            onDismissRequest = { showUninstallDialog = false },
            title = { Text(i18n.text("ui.mods.uninstall")) },
            text = { Text("${i18n.text("ui.mods.confirm_uninstall")} ${mod.name}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            uninstallMod(mod)
                            showUninstallDialog = false
                        }
                    }
                ) {
                    Text(i18n.text("ui.mods.uninstall"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showUninstallDialog = false }) {
                    Text(i18n.text("ui.cancel"))
                }
            }
        )
    }
}

// 工具函数
suspend fun checkIfModInstalled(modName: String): Boolean {
    val modsDir = Paths.get(ResourceManager.ASSETS_FOLDER, "versions", "mods").toFile()
    if (!modsDir.exists()) return false
    
    val modFiles = modsDir.listFiles { file -> file.extension == "jar" } ?: return false
    return modFiles.any { file -> 
        file.name.contains(modName, ignoreCase = true) || 
        file.nameWithoutExtension.contains(modName, ignoreCase = true)
    }
}

suspend fun downloadMod(mod: ModrinthProject) {
    try {
        // 获取项目详情
        val projectDetails = ModrinthAPI.getProject(mod.projectId)
        if (projectDetails == null) {
            println("Failed to get project details")
            return
        }
        
        // 获取版本信息
        val versions = ModrinthAPI.getProjectVersions(mod.projectId)
        if (versions.isEmpty()) {
            println("No versions found")
            return
        }
        
        // 查找适用于fabric 1.21.4的版本
        val compatibleVersion = versions.find { version ->
            "fabric" in version.loaders && "1.21.4" in version.gameVersions
        } ?: versions.firstOrNull()
        
        if (compatibleVersion == null) {
            println("No compatible version found")
            return
        }
        
        // 获取主文件
        val primaryFile = compatibleVersion.files.find { it.primary } ?: compatibleVersion.files.firstOrNull()
        if (primaryFile == null) {
            println("No download file found")
            return
        }
        
        // 下载文件
        val modsDir = Paths.get(ResourceManager.ASSETS_FOLDER, "versions", "mods").toFile()
        if (!modsDir.exists()) {
            modsDir.mkdirs()
        }
        
        val outputFile = File(modsDir, primaryFile.filename)
        downloadFile(primaryFile.url, outputFile) { _, _ -> }
        
        println("Downloaded ${mod.title} successfully")
    } catch (e: Exception) {
        println("Failed to download mod: ${e.message}")
        e.printStackTrace()
    }
}

fun downloadFile(url: String, outputFile: File, onProgress: (Float, String) -> Unit) {
    var lastException: Exception? = null

    // 尝试下载3次
    for (attempt in 1..3) {
        try {
            val connection = URI.create(url).toURL().openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 30000
            connection.readTimeout = 30000
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")

            val totalSize = connection.contentLength

            BufferedInputStream(connection.inputStream).use { input ->
                FileOutputStream(outputFile).use { output ->
                    val buffer = ByteArray(8192)
                    var downloaded = 0
                    var bytesRead: Int

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloaded += bytesRead

                        if (totalSize > 0) {
                            val progress = downloaded.toFloat() / totalSize
                            onProgress(progress, "downloading... ${(progress * 100).toInt()}%")
                        }
                    }
                }
            }

            // 下载成功，直接返回
            return
        } catch (e: Exception) {
            lastException = e
            println("Attempt $attempt failed to download $url: ${e.message}")

            // 如果不是最后一次尝试，等待一段时间再重试
            if (attempt < 3) {
                try {
                    Thread.sleep(1000 * attempt.toLong())
                } catch (ie: InterruptedException) {
                    Thread.currentThread().interrupt()
                    throw ie
                }
            }
        }
    }

    // 如果所有尝试都失败了，抛出最后一个异常
    throw lastException ?: RuntimeException("Failed to download $url after 3 attempts")
}

suspend fun getInstalledMods(): List<LocalModInfo> {
    val mods = mutableListOf<LocalModInfo>()
    
    val modsDir = Paths.get(ResourceManager.ASSETS_FOLDER, "versions", "mods").toFile()
    if (!modsDir.exists()) return emptyList()
    
    val modFiles = modsDir.listFiles { file -> 
        file.extension == "jar" && file.name != "soarclient-fork-8.0.0.jar" && file.name != "fabric-api-0.119.4+1.21.4.jar"
    } ?: return emptyList()
    
    for (jarFile in modFiles) {
        try {
            // 尝试从jar文件中读取fabric.mod.json
            val modInfo = readFabricModJsonFromJar(jarFile)
            if (modInfo != null) {
                // 使用反射设置瞬时字段
                modInfo.javaClass.getDeclaredField("jarFile").apply {
                    isAccessible = true
                    set(modInfo, jarFile)
                }
                modInfo.javaClass.getDeclaredField("isInstalled").apply {
                    isAccessible = true
                    set(modInfo, true)
                }
                mods.add(modInfo)
            }
        } catch (e: Exception) {
            println("Failed to read mod info from ${jarFile.name}: ${e.message}")
        }
    }
    
    // 添加Fabric API作为受保护的mod
    val fabricApiJar = File(modsDir, "fabric-api-0.119.4+1.21.4.jar")
    if (fabricApiJar.exists()) {
        try {
            val fabricMod = LocalModInfo(
                id = "fabric-api",
                name = "Fabric API",
                version = "0.119.4+1.21.4",
                description = "Essential hooks and interoperability mechanisms for Fabric mods",
                authors = listOf("FabricMC"),
                license = "Apache-2.0"
            )
            // 使用反射设置瞬时字段
            fabricMod.javaClass.getDeclaredField("jarFile").apply {
                isAccessible = true
                set(fabricMod, fabricApiJar)
            }
            fabricMod.javaClass.getDeclaredField("isInstalled").apply {
                isAccessible = true
                set(fabricMod, true)
            }
            mods.add(0, fabricMod) // 添加到列表开头
        } catch (e: Exception) {
            println("Failed to add Fabric API to mod list: ${e.message}")
        }
    }
    
    // 添加SoarClient-fork作为受保护的mod
    val soarClientJar = File(modsDir, "soarclient-fork-8.0.0.jar")
    if (soarClientJar.exists()) {
        try {
            val soarMod = LocalModInfo(
                id = "soarclient",
                name = "SoarClient-fork",
                version = "8.0.0",
                description = "Modern and powerful client for Minecraft PvP",
                authors = listOf("EldoDebug", "Eatgrapes", "CubeWhy", "LazyChara")
            )
            // 使用反射设置瞬时字段
            soarMod.javaClass.getDeclaredField("jarFile").apply {
                isAccessible = true
                set(soarMod, soarClientJar)
            }
            soarMod.javaClass.getDeclaredField("isInstalled").apply {
                isAccessible = true
                set(soarMod, true)
            }
            mods.add(0, soarMod) // 添加到列表开头
        } catch (e: Exception) {
            println("Failed to add SoarClient to mod list: ${e.message}")
        }
    }
    
    return mods
}

fun readFabricModJsonFromJar(jarFile: File): LocalModInfo? {
    try {
        java.util.jar.JarFile(jarFile).use { jar ->
            val modJsonEntry = jar.getJarEntry("fabric.mod.json")
            if (modJsonEntry != null) {
                jar.getInputStream(modJsonEntry).use { input ->
                    val jsonContent = input.bufferedReader().readText()
                    val mapper = com.fasterxml.jackson.databind.ObjectMapper()
                    return mapper.readValue(jsonContent, LocalModInfo::class.java)
                }
            }
        }
    } catch (e: Exception) {
        println("Error reading fabric.mod.json from ${jarFile.name}: ${e.message}")
    }
    return null
}

suspend fun uninstallMod(mod: LocalModInfo) {
    try {
        // 使用反射获取jarFile字段
        val jarFileField = mod.javaClass.getDeclaredField("jarFile")
        jarFileField.isAccessible = true
        val jarFile = jarFileField.get(mod) as File
        
        if (jarFile.exists()) {
            jarFile.delete()
            println("Uninstalled ${mod.name} successfully")
        }
    } catch (e: Exception) {
        println("Failed to uninstall ${mod.name}: ${e.message}")
    }
}

fun loadOnlineImage(url: String): ImageBitmap? {
    return try {
        val connection = URI.create(url).toURL().openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 5000
        connection.readTimeout = 5000
        connection.setRequestProperty("User-Agent", "SoarClient-fork_Launcher/1.0")
        
        val inputStream = connection.inputStream
        val image = readImageFromStream(inputStream)
        inputStream.close()
        
        // 检查图片是否成功加载
        if (image == null) {
            println("Failed to load image from URL: $url - ImageIO.read returned null")
            return null
        }
        
        // 转换为Compose ImageBitmap
        val bufferedImage = BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_ARGB)
        val graphics = bufferedImage.createGraphics()
        graphics.drawImage(image, 0, 0, null)
        graphics.dispose()
        
        // 转换为字节数组
        val baos = ByteArrayOutputStream()
        ImageIO.write(bufferedImage, "png", baos)
        val bytes = baos.toByteArray()
        baos.close()
        
        // 创建ImageBitmap
        val decodedImage = Image.makeFromEncoded(bytes)
        decodedImage.toComposeImageBitmap()
    } catch (e: Exception) {
        println("Failed to load online image from URL: $url - ${e.message}")
        null
    }
}

fun loadImageFromJar(mod: LocalModInfo): ImageBitmap? {
    return try {
        if (mod.icon == null || mod.icon.isEmpty()) return null
        
        ZipFile(mod.jarFile).use { zip ->
            val iconEntry = zip.getEntry(mod.icon)
            if (iconEntry != null) {
                zip.getInputStream(iconEntry).use { input ->
                    val image = readImageFromStream(input)
                    // 检查图片是否成功加载
                    if (image == null) {
                        println("Failed to load image from jar: ${mod.jarFile.name} - ImageIO.read returned null for icon path: ${mod.icon}")
                        return null
                    }
                    
                    // 转换为Compose ImageBitmap
                    val bufferedImage = BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_ARGB)
                    val graphics = bufferedImage.createGraphics()
                    graphics.drawImage(image, 0, 0, null)
                    graphics.dispose()
                    
                    // 转换为字节数组
                    val baos = ByteArrayOutputStream()
                    ImageIO.write(bufferedImage, "png", baos)
                    val bytes = baos.toByteArray()
                    baos.close()
                    
                    // 创建ImageBitmap
                    val decodedImage = Image.makeFromEncoded(bytes)
                    return decodedImage.toComposeImageBitmap()
                }
            } else {
                println("Icon entry not found in jar: ${mod.jarFile.name} for icon path: ${mod.icon}")
            }
        }
        null
    } catch (e: Exception) {
        println("Failed to load image from jar: ${mod.jarFile.name} - ${e.message}")
        null
    }
}

fun readImageFromStream(inputStream: InputStream): BufferedImage? {
    try {
        // 注册WebP imageio插件
        try {
            // 尝试注册Twelvemonkeys WebP支持
            val registry = IIORegistry.getDefaultInstance()
            val webpReaderSpi = Class.forName("com.twelvemonkeys.imageio.plugins.webp.WebPImageReaderSpi")
                .getDeclaredConstructor()
                .newInstance() as ImageReaderSpi
            registry.registerServiceProvider(webpReaderSpi)
        } catch (e: Exception) {
            // WebP支持不可用，继续使用标准ImageIO
            println("WebP support not available or failed to register: ${e.message}")
        }
        
        // 尝试使用标准ImageIO读取
        val image = ImageIO.read(inputStream)
        if (image != null) {
            return image
        }
    } catch (e: Exception) {
        println("Standard ImageIO failed: ${e.message}")
    }
    
    return null
}