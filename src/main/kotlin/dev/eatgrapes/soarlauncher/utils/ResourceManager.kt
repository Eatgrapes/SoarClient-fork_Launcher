package dev.eatgrapes.soarlauncher.utils

import java.io.*
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import java.util.zip.GZIPInputStream
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ResourceManager {
    private const val JAVA_VERSION = "21.0.7"
    private const val ASSETS_FOLDER = "soar_assets"
    
    private val WINDOWS_URL = "https://download.oracle.com/java/21/archive/jdk-${JAVA_VERSION}_windows-x64_bin.zip"
    private val LINUX_URL = "https://download.oracle.com/java/21/archive/jdk-${JAVA_VERSION}_linux-x64_bin.tar.gz"
    private val MAC_URL = "https://download.oracle.com/java/21/archive/jdk-${JAVA_VERSION}_macos-x64_bin.tar.gz"
    
    private const val MINECRAFT_JAR_URL = "https://eatgrapes.github.io/Soar-fork_Web/Mod/1.21.4-soar.jar"
    private const val MINECRAFT_JSON_URL = "https://eatgrapes.github.io/Soar-fork_Web/Mod/1.21.4-soar.json"
    private const val VERSION_INFO_URL = "https://eatgrapes.github.io/Soar-fork_Web/Mod/version.json"
    private const val SOAR_CLIENT_JAR_URL = "https://eatgrapes.github.io/Soar-fork_Web/Mod/soarclient-fork-8.0.0.jar"
    
    enum class DownloadState {
        IDLE, CHECKING, DOWNLOADING, EXTRACTING, COMPLETE, ERROR
    }
    
    data class DownloadProgress(
        val state: DownloadState,
        val progress: Float = 0f,
        val message: String = ""
    )
    
    private var _currentProgress = DownloadProgress(DownloadState.IDLE)
    val currentProgress get() = _currentProgress
    
    fun getJavaExecutablePath(): String? {
        val assetsPath = Paths.get(ASSETS_FOLDER).toAbsolutePath()
        val javaDir = assetsPath.resolve("jdk-$JAVA_VERSION")
        
        if (!Files.exists(javaDir)) return null
        
        return when {
            System.getProperty("os.name").lowercase().contains("win") -> 
                javaDir.resolve("bin").resolve("java.exe").toString()
            else -> 
                javaDir.resolve("bin").resolve("java").toString()
        }
    }
    
    fun isJava21Installed(): Boolean {
        val javaPath = getJavaExecutablePath()
        return javaPath != null && File(javaPath).exists()
    }
    
    fun areMinecraftFilesDownloaded(): Boolean {
        val versionPath = Paths.get(ASSETS_FOLDER, "versions").toAbsolutePath()
        val jarFile = versionPath.resolve("1.21.4-soar.jar").toFile()
        val jsonFile = versionPath.resolve("1.21.4-soar.json").toFile()
        return jarFile.exists() && jsonFile.exists()
    }
    
    suspend fun downloadJava21(onProgressUpdate: (DownloadProgress) -> Unit) {
        if (isJava21Installed()) {
            _currentProgress = DownloadProgress(DownloadState.COMPLETE, 1f, "Java 21已安装")
            onProgressUpdate(_currentProgress)
            checkAndDownloadVersionInfo(onProgressUpdate)
            downloadMinecraftFiles(onProgressUpdate)
            return
        }
        
        try {
            _currentProgress = DownloadProgress(DownloadState.CHECKING, 0f, "检查资源...")
            onProgressUpdate(_currentProgress)
            
            val downloadUrl = when {
                System.getProperty("os.name").lowercase().contains("win") -> WINDOWS_URL
                System.getProperty("os.name").lowercase().contains("mac") -> MAC_URL
                else -> LINUX_URL
            }
            
            val assetsPath = Paths.get(ASSETS_FOLDER).toAbsolutePath()
            Files.createDirectories(assetsPath)
            
            val fileName = when {
                downloadUrl.contains("windows") -> "jdk-${JAVA_VERSION}_windows.zip"
                downloadUrl.contains("macos") -> "jdk-${JAVA_VERSION}_macos.tar.gz"
                else -> "jdk-${JAVA_VERSION}_linux.tar.gz"
            }
            
            val downloadFile = assetsPath.resolve(fileName).toFile()
            
            _currentProgress = DownloadProgress(DownloadState.DOWNLOADING, 0f, "下载Java 21...")
            onProgressUpdate(_currentProgress)
            
            withContext(Dispatchers.IO) {
                downloadFile(downloadUrl, downloadFile) { progress, message ->
                    _currentProgress = DownloadProgress(DownloadState.DOWNLOADING, progress, message)
                    onProgressUpdate(_currentProgress)
                }
                
                _currentProgress = DownloadProgress(DownloadState.EXTRACTING, 0f, "解压Java 21...")
                onProgressUpdate(_currentProgress)
                
                extractArchive(downloadFile, assetsPath.toFile())
                
                downloadFile.delete()
            }
            
            _currentProgress = DownloadProgress(DownloadState.COMPLETE, 1f, "Java 21安装完成")
            onProgressUpdate(_currentProgress)
            
            downloadMinecraftFiles(onProgressUpdate)
            
        } catch (e: Exception) {
            _currentProgress = DownloadProgress(DownloadState.ERROR, 0f, "错误: ${e.message}")
            onProgressUpdate(_currentProgress)
        }
    }
    
    suspend fun checkAndDownloadVersionInfo(onProgressUpdate: (DownloadProgress) -> Unit) {
        try {
            val versionPath = Paths.get(ASSETS_FOLDER, "versions").toAbsolutePath()
            Files.createDirectories(versionPath)
            
            val localVersionFile = versionPath.resolve("version.json").toFile()
            val remoteVersionContent = downloadVersionInfo()
            
            if (remoteVersionContent != null) {
                val localContent = if (localVersionFile.exists()) localVersionFile.readText() else ""
                
                if (remoteVersionContent != localContent || !localVersionFile.exists()) {
                    _currentProgress = DownloadProgress(DownloadState.DOWNLOADING, 0f, "更新版本信息...")
                    onProgressUpdate(_currentProgress)
                    
                    localVersionFile.writeText(remoteVersionContent)
                    
                    downloadSoarClientJar(onProgressUpdate)
                } else {
                    _currentProgress = DownloadProgress(DownloadState.COMPLETE, 1f, "版本信息已是最新")
                    onProgressUpdate(_currentProgress)
                }
            } else {
                _currentProgress = DownloadProgress(DownloadState.ERROR, 0f, "无法获取远程版本信息")
                onProgressUpdate(_currentProgress)
            }
        } catch (e: Exception) {
            _currentProgress = DownloadProgress(DownloadState.ERROR, 0f, "版本检查错误: ${e.message}")
            onProgressUpdate(_currentProgress)
        }
    }
    
    private suspend fun downloadVersionInfo(): String? {
        return withContext(Dispatchers.IO) {
            try {
                val connection = URI.create(VERSION_INFO_URL).toURL().openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                
                connection.inputStream.bufferedReader().use { it.readText() }
            } catch (e: Exception) {
                null
            }
        }
    }
    
    private suspend fun downloadSoarClientJar(onProgressUpdate: (DownloadProgress) -> Unit) {
        try {
            val modsPath = Paths.get(ASSETS_FOLDER, "versions", "mods").toAbsolutePath()
            Files.createDirectories(modsPath)
            
            val jarFile = modsPath.resolve("soarclient-fork-8.0.0.jar").toFile()
            
            _currentProgress = DownloadProgress(DownloadState.DOWNLOADING, 0f, "下载Soar客户端...")
            onProgressUpdate(_currentProgress)
            
            withContext(Dispatchers.IO) {
                downloadFile(SOAR_CLIENT_JAR_URL, jarFile) { progress, message ->
                    _currentProgress = DownloadProgress(DownloadState.DOWNLOADING, progress, message)
                    onProgressUpdate(_currentProgress)
                }
            }
            
            _currentProgress = DownloadProgress(DownloadState.COMPLETE, 1f, "Soar客户端下载完成")
            onProgressUpdate(_currentProgress)
            
        } catch (e: Exception) {
            _currentProgress = DownloadProgress(DownloadState.ERROR, 0f, "Soar客户端下载错误: ${e.message}")
            onProgressUpdate(_currentProgress)
        }
    }
    
    suspend fun downloadMinecraftFiles(onProgressUpdate: (DownloadProgress) -> Unit) {
        if (areMinecraftFilesDownloaded()) {
            _currentProgress = DownloadProgress(DownloadState.COMPLETE, 1f, "Minecraft文件已存在")
            onProgressUpdate(_currentProgress)
            return
        }
        
        try {
            _currentProgress = DownloadProgress(DownloadState.CHECKING, 0f, "检查Minecraft文件...")
            onProgressUpdate(_currentProgress)
            
            val versionPath = Paths.get(ASSETS_FOLDER, "versions").toAbsolutePath()
            Files.createDirectories(versionPath)
            
            val jarFile = versionPath.resolve("1.21.4-soar.jar").toFile()
            val jsonFile = versionPath.resolve("1.21.4-soar.json").toFile()
            
            _currentProgress = DownloadProgress(DownloadState.DOWNLOADING, 0f, "下载Minecraft文件...")
            onProgressUpdate(_currentProgress)
            
            withContext(Dispatchers.IO) {
                downloadFile(MINECRAFT_JAR_URL, jarFile) { progress, message ->
                    _currentProgress = DownloadProgress(DownloadState.DOWNLOADING, progress * 0.5f, message)
                    onProgressUpdate(_currentProgress)
                }
                
                downloadFile(MINECRAFT_JSON_URL, jsonFile) { progress, message ->
                    _currentProgress = DownloadProgress(DownloadState.DOWNLOADING, 0.5f + progress * 0.5f, message)
                    onProgressUpdate(_currentProgress)
                }
            }
            
            _currentProgress = DownloadProgress(DownloadState.COMPLETE, 1f, "Minecraft文件下载完成")
            onProgressUpdate(_currentProgress)
            
        } catch (e: Exception) {
            _currentProgress = DownloadProgress(DownloadState.ERROR, 0f, "错误: ${e.message}")
            onProgressUpdate(_currentProgress)
        }
    }
    
    private fun downloadFile(url: String, outputFile: File, onProgress: (Float, String) -> Unit) {
        val connection = URI.create(url).toURL().openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 30000
        connection.readTimeout = 30000
        
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
    }
    
    private fun extractArchive(archiveFile: File, outputDir: File) {
        when {
            archiveFile.name.endsWith(".zip") -> extractZip(archiveFile, outputDir)
            archiveFile.name.endsWith(".tar.gz") -> extractTarGz(archiveFile, outputDir)
        }
    }
    
    private fun extractZip(zipFile: File, outputDir: File) {
        ZipFile(zipFile).use { zip ->
            zip.entries().asSequence().forEach { entry ->
                val file = File(outputDir, entry.name)
                if (entry.isDirectory) {
                    file.mkdirs()
                } else {
                    file.parentFile?.mkdirs()
                    zip.getInputStream(entry).use { input ->
                        FileOutputStream(file).use { output ->
                            input.copyTo(output)
                        }
                    }
                }
            }
        }
    }
    
    private fun extractTarGz(tarGzFile: File, outputDir: File) {
        FileInputStream(tarGzFile).use { fis ->
            GZIPInputStream(fis).use { gzip ->
                TarArchiveInputStream(gzip).use { tar ->
                    var entry = tar.nextEntry
                    while (entry != null) {
                        val file = File(outputDir, entry.name)
                        if (entry.isDirectory) {
                            file.mkdirs()
                        } else {
                            file.parentFile?.mkdirs()
                            FileOutputStream(file).use { output ->
                                tar.copyTo(output)
                            }
                            if (entry.mode and 0b111101101 != 0) {
                                file.setExecutable(true)
                            }
                        }
                        entry = tar.nextEntry
                    }
                }
            }
        }
    }
}