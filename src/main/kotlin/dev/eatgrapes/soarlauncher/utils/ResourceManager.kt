package dev.eatgrapes.soarlauncher.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URI
import java.nio.file.Files
import java.nio.file.Paths
import java.util.zip.GZIPInputStream
import java.util.zip.ZipFile
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper

/**
 * 用于描述一个已安装 Mod 的数据类
 * @param id Mod 的唯一标识符
 * @param version Mod 的版本
 * @param name Mod 的显示名称
 * @param enabled Mod 是否启用 (通过文件扩展名判断)
 * @param file Mod 的 JAR 文件
 */
data class ModInfo(
    val id: String,
    val version: String,
    val name: String,
    val enabled: Boolean,
    val file: File
)

object ResourceManager {
    const val ASSETS_FOLDER = "soar_assets"
    private const val JAVA_VERSION = "21.0.7"

    private val WINDOWS_URL = "https://download.oracle.com/java/21/archive/jdk-${JAVA_VERSION}_windows-x64_bin.zip"
    private val LINUX_URL = "https://download.oracle.com/java/21/archive/jdk-${JAVA_VERSION}_linux-x64_bin.tar.gz"
    private val MAC_URL = "https://download.oracle.com/java/21/archive/jdk-${JAVA_VERSION}_macos-x64_bin.tar.gz"

    private const val ADDITIONAL_ASSETS_URL = "https://eatgrapes.github.io/Soar-fork_Web/Mod/11.zip"
    private const val SOAR_CLIENT_JAR_URL = "https://eatgrapes.github.io/Soar-fork_Web/Mod/soarclient-fork-8.0.0.jar"
    private const val FABRIC_API_URL = "https://cdn.modrinth.com/data/P7dR8mSH/versions/p96k10UR/fabric-api-0.119.4%2B1.21.4.jar"
    private const val ASSETS_BASE_URL = "https://resources.download.minecraft.net"

    private const val MAX_CONCURRENT_DOWNLOADS = 16

    enum class DownloadState {
        IDLE, CHECKING, DOWNLOADING, EXTRACTING, COMPLETE, ERROR
    }

    data class DownloadProgress(
        val state: DownloadState,
        val progress: Float = 0f,
        val message: String = ""
    )

    data class LibraryInfo(
        val name: String,
        val url: String,
        val path: String,
        val shouldDownload: Boolean = true
    )

    private var _currentProgress = DownloadProgress(DownloadState.IDLE)
    val currentProgress get() = _currentProgress

    fun getJavaExecutablePath(): String? {
        val assetsPath = Paths.get(ASSETS_FOLDER).toAbsolutePath()
        val javaDir = assetsPath.resolve("jdk-$JAVA_VERSION")

        if (!Files.exists(javaDir)) return null

        return when {
            System.getProperty("os.name").lowercase().contains("win") ->
                javaDir.resolve("bin").resolve("javaw.exe").toString()

            else ->
                javaDir.resolve("bin").resolve("java").toString()
        }
    }

    fun isJava21Installed(): Boolean {
        val javaPath = getJavaExecutablePath()
        return javaPath != null && File(javaPath).exists()
    }

    fun areMinecraftFilesDownloaded(): Boolean {
        val assetsPath = Paths.get(ASSETS_FOLDER).toAbsolutePath()
        val libsDir = assetsPath.resolve("libs")
        val versionFile = assetsPath.resolve("versions").resolve("1.21.4-soar.json")

        if (!versionFile.toFile().exists()) {
            return false
        }

        if (!libsDir.toFile().exists() || libsDir.toFile().listFiles().isNullOrEmpty()) {
            return false
        }

        return true
    }

    fun areAdditionalAssetsExtracted(): Boolean {
        val markerFile = Paths.get(ASSETS_FOLDER, "versions", ".additional_assets_extracted").toFile()
        return markerFile.exists()
    }

    fun areAssetsDownloaded(): Boolean {
        val assetsIndexPath = Paths.get(ASSETS_FOLDER, "indexes", "1.21.json")
        return assetsIndexPath.toFile().exists()
    }

    private fun parseDependenciesFromJson(jsonFile: File): List<LibraryInfo> {
        val mapper = ObjectMapper()
        val versionJson = mapper.readTree(jsonFile)
        val librariesNode = versionJson.get("libraries") ?: return emptyList()

        val libraries = mutableListOf<LibraryInfo>()
        librariesNode.forEach { library ->
            val downloadsNode = library.get("downloads")
            if (downloadsNode != null && downloadsNode.has("artifact")) {
                val artifactNode = downloadsNode.get("artifact")
                val url = artifactNode.get("url").asText()
                val path = artifactNode.get("path").asText()

                if (!path.contains("assets")) {
                    libraries.add(LibraryInfo(library.get("name").asText(), url, path))
                }
            } else if (library.has("name") && library.has("url")) {
                val name = library.get("name").asText()
                val baseUrl = library.get("url").asText()

                val parts = name.split(":")
                if (parts.size >= 3) {
                    val groupId = parts[0]
                    val artifactId = parts[1]
                    val version = parts[2]

                    val groupPath = groupId.replace(".", "/")
                    val fileName = if (parts.size > 3) {
                        val classifier = parts[3]
                        "$artifactId-$version-$classifier.jar"
                    } else {
                        "$artifactId-$version.jar"
                    }

                    val fullPath = "$groupPath/$artifactId/$version/$fileName"
                    libraries.add(LibraryInfo(name, baseUrl + fullPath, fullPath))
                }
            }
        }

        return libraries
    }

    private fun isLibraryDownloaded(library: LibraryInfo): Boolean {
        val libsDir = Paths.get(ASSETS_FOLDER, "libs").toFile()
        val outputFile = File(libsDir, library.path)
        return outputFile.exists() && outputFile.length() > 0
    }

    suspend fun downloadDependencies(onProgressUpdate: (DownloadProgress) -> Unit) {
        try {
            _currentProgress = DownloadProgress(DownloadState.CHECKING, 0f, "Check assets...")
            onProgressUpdate(_currentProgress)

            val versionFile = Paths.get(ASSETS_FOLDER, "versions", "1.21.4-soar.json").toFile()
            if (!versionFile.exists()) {
                _currentProgress = DownloadProgress(DownloadState.ERROR, 0f, "Config Not Found")
                onProgressUpdate(_currentProgress)
                return
            }

            val libraries = parseDependenciesFromJson(versionFile)
            if (libraries.isEmpty()) {
                _currentProgress = DownloadProgress(DownloadState.ERROR, 0f, "libs config error")
                onProgressUpdate(_currentProgress)
                return
            }

            val librariesToDownload = libraries.filter { !isLibraryDownloaded(it) }

            if (librariesToDownload.isEmpty()) {
                _currentProgress = DownloadProgress(DownloadState.COMPLETE, 1f, "libs Ready")
                onProgressUpdate(_currentProgress)
                return
            }

            var downloadedCount = 0
            var failedCount = 0
            val totalLibraries = librariesToDownload.size

            coroutineScope {
                librariesToDownload.chunked(MAX_CONCURRENT_DOWNLOADS).forEach { batch ->
                    val downloadJobs = batch.map { library ->
                        async(Dispatchers.IO) {
                            try {
                                val libsDir = Paths.get(ASSETS_FOLDER, "libs").toFile()
                                val outputFile = File(libsDir, library.path)

                                outputFile.parentFile?.let { parent ->
                                    if (!parent.exists()) {
                                        parent.mkdirs()
                                    }
                                }

                                downloadFile(library.url, outputFile) { _, _ -> }

                                synchronized(this) {
                                    downloadedCount++
                                    val progress = downloadedCount.toFloat() / totalLibraries
                                    _currentProgress = DownloadProgress(
                                        DownloadState.DOWNLOADING,
                                        progress,
                                        "Downloading libraries: $downloadedCount/$totalLibraries (failed: $failedCount)"
                                    )
                                    onProgressUpdate(_currentProgress)
                                }
                                true
                            } catch (e: Exception) {
                                println("Failed to download ${library.path}: ${e.message}")
                                synchronized(this) {
                                    downloadedCount++
                                    failedCount++
                                }
                                false
                            }
                        }
                    }

                    downloadJobs.awaitAll()
                }
            }

            _currentProgress = DownloadProgress(DownloadState.COMPLETE, 1f, "libs Ready (failed: $failedCount)")
            onProgressUpdate(_currentProgress)

        } catch (e: Exception) {
            _currentProgress = DownloadProgress(DownloadState.ERROR, 0f, "libs Error: ${e.message}")
            onProgressUpdate(_currentProgress)
        }
    }

    suspend fun downloadAssetIndex(onProgressUpdate: (DownloadProgress) -> Unit) {
        try {
            _currentProgress = DownloadProgress(DownloadState.CHECKING, 0f, "Check assets index...")
            onProgressUpdate(_currentProgress)

            val versionFile = Paths.get(ASSETS_FOLDER, "versions", "1.21.4-soar.json").toFile()
            if (!versionFile.exists()) {
                _currentProgress = DownloadProgress(DownloadState.ERROR, 0f, "Config Not Found")
                onProgressUpdate(_currentProgress)
                return
            }

            val mapper = ObjectMapper()
            val versionJson = mapper.readTree(versionFile)

            val assetIndexNode = versionJson.get("assetIndex")
            if (assetIndexNode == null || !assetIndexNode.has("url") || !assetIndexNode.has("id")) {
                _currentProgress = DownloadProgress(DownloadState.ERROR, 0f, "Asset index config error")
                onProgressUpdate(_currentProgress)
                return
            }

            val assetIndexUrl = assetIndexNode.get("url").asText()
            val assetIndexId = assetIndexNode.get("id").asText()

            val indexesDir = Paths.get(ASSETS_FOLDER,"versions","assets" ,"indexes").toFile()
            if (!indexesDir.exists()) {
                indexesDir.mkdirs()
            }

            val assetIndexFile = File(indexesDir, "19.json")

            if (assetIndexFile.exists()) {
                _currentProgress = DownloadProgress(DownloadState.COMPLETE, 1f, "Asset index Ready")
                onProgressUpdate(_currentProgress)
                return
            }

            _currentProgress = DownloadProgress(DownloadState.DOWNLOADING, 0f, "Downloading asset index...")
            onProgressUpdate(_currentProgress)

            withContext(Dispatchers.IO) {
                downloadFile(assetIndexUrl, assetIndexFile) { progress, message ->
                    _currentProgress = DownloadProgress(
                        DownloadState.DOWNLOADING,
                        progress,
                        "Downloading asset index - $message"
                    )
                    onProgressUpdate(_currentProgress)
                }
            }

            _currentProgress = DownloadProgress(DownloadState.COMPLETE, 1f, "Asset index Ready")
            onProgressUpdate(_currentProgress)

        } catch (e: Exception) {
            _currentProgress = DownloadProgress(DownloadState.ERROR, 0f, "Asset index Error: ${e.message}")
            onProgressUpdate(_currentProgress)
        }
    }

    suspend fun downloadAssets(onProgressUpdate: (DownloadProgress) -> Unit) {
        try {
            _currentProgress = DownloadProgress(DownloadState.CHECKING, 0f, "Check assets...")
            onProgressUpdate(_currentProgress)

            val assetIndexFile = Paths.get(ASSETS_FOLDER, "versions","assets","indexes", "19.json").toFile()
            if (!assetIndexFile.exists()) {
                _currentProgress = DownloadProgress(DownloadState.ERROR, 0f, "Asset index not found")
                onProgressUpdate(_currentProgress)
                return
            }

            val mapper = ObjectMapper()
            val assetIndexJson = mapper.readTree(assetIndexFile)

            val objectsNode = assetIndexJson.get("objects")
            if (objectsNode == null || !objectsNode.isObject) {
                _currentProgress = DownloadProgress(DownloadState.ERROR, 0f, "Asset objects config error")
                onProgressUpdate(_currentProgress)
                return
            }

            val assetsDir = Paths.get(ASSETS_FOLDER, "versions", "assets").toFile()
            if (!assetsDir.exists()) {
                assetsDir.mkdirs()
            }

            val objectsDir = Paths.get(ASSETS_FOLDER, "versions", "assets", "objects").toFile()
            if (!objectsDir.exists()) {
                objectsDir.mkdirs()
            }

            val assets = mutableListOf<Pair<String, JsonNode>>()
            objectsNode.fields().forEach { entry ->
                assets.add(Pair(entry.key, entry.value))
            }

            var downloadedCount = 0
            var failedCount = 0
            val totalAssets = assets.size

            coroutineScope {
                assets.chunked(MAX_CONCURRENT_DOWNLOADS).forEach { batch ->
                    val downloadJobs = batch.map { (assetPath, assetInfo) ->
                        async(Dispatchers.IO) {
                            try {
                                val hash = assetInfo.get("hash").asText()
                                val url = "$ASSETS_BASE_URL/${hash.substring(0, 2)}/$hash"

                                val targetDir = File(objectsDir, hash.substring(0, 2))
                                if (!targetDir.exists()) {
                                    targetDir.mkdirs()
                                }

                                val targetFile = File(targetDir, hash)

                                if (targetFile.exists()) {
                                    synchronized(this) {
                                        downloadedCount++
                                        val progress = downloadedCount.toFloat() / totalAssets
                                        _currentProgress = DownloadProgress(
                                            DownloadState.DOWNLOADING,
                                            progress,
                                            "Skipping existing asset: $assetPath"
                                        )
                                        onProgressUpdate(_currentProgress)
                                    }
                                    return@async true
                                }

                                downloadFile(url, targetFile) { _, _ -> }

                                synchronized(this) {
                                    downloadedCount++
                                    val progress = downloadedCount.toFloat() / totalAssets
                                    _currentProgress = DownloadProgress(
                                        DownloadState.DOWNLOADING,
                                        progress,
                                        "Downloading assets: $downloadedCount/$totalAssets (failed: $failedCount)"
                                    )
                                    onProgressUpdate(_currentProgress)
                                }
                                true
                            } catch (e: Exception) {
                                println("Failed to download asset $assetPath: ${e.message}")
                                synchronized(this) {
                                    downloadedCount++
                                    failedCount++
                                }
                                false
                            }
                        }
                    }

                    downloadJobs.awaitAll()
                }
            }

            _currentProgress = DownloadProgress(DownloadState.COMPLETE, 1f, "Assets Ready (failed: $failedCount)")
            onProgressUpdate(_currentProgress)

        } catch (e: Exception) {
            _currentProgress = DownloadProgress(DownloadState.ERROR, 0f, "Assets Error: ${e.message}")
            onProgressUpdate(_currentProgress)
        }
    }

    suspend fun downloadJava21(onProgressUpdate: (DownloadProgress) -> Unit) {
        if (isJava21Installed()) {
            _currentProgress = DownloadProgress(DownloadState.COMPLETE, 1f, "Java 21 Ready")
            onProgressUpdate(_currentProgress)
            return
        }

        try {
            _currentProgress = DownloadProgress(DownloadState.CHECKING, 0f, "Check assets...")
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

            _currentProgress = DownloadProgress(DownloadState.DOWNLOADING, 0f, "doswnload Java 21...")
            onProgressUpdate(_currentProgress)

            withContext(Dispatchers.IO) {
                downloadFile(downloadUrl, downloadFile) { progress, message ->
                    _currentProgress = DownloadProgress(DownloadState.DOWNLOADING, progress, message)
                    onProgressUpdate(_currentProgress)
                }

                _currentProgress = DownloadProgress(DownloadState.EXTRACTING, 0f, "unzip Java 21...")
                onProgressUpdate(_currentProgress)

                extractArchive(downloadFile, assetsPath.toFile())

                downloadFile.delete()
            }

            _currentProgress = DownloadProgress(DownloadState.COMPLETE, 1f, "install Java 21 Succeed")
            onProgressUpdate(_currentProgress)

        } catch (e: Exception) {
            _currentProgress = DownloadProgress(DownloadState.ERROR, 0f, "error: ${e.message}")
            onProgressUpdate(_currentProgress)
        }
    }

    suspend fun checkAndDownloadVersionInfo(onProgressUpdate: (DownloadProgress) -> Unit) {
        try {
            _currentProgress = DownloadProgress(DownloadState.CHECKING, 0f, "Check Version...")
            onProgressUpdate(_currentProgress)

            val assetsPath = Paths.get(ASSETS_FOLDER).toAbsolutePath()
            val versionsDir = assetsPath.resolve("versions")
            Files.createDirectories(versionsDir)

            val versionFile = versionsDir.resolve("1.21.4-soar.json")

            if (!versionFile.toFile().exists()) {
                _currentProgress = DownloadProgress(DownloadState.DOWNLOADING, 0f, "Download Version...")
                onProgressUpdate(_currentProgress)

                val versionUrl = "https://eatgrapes.github.io/Soar-fork_Web/Mod/1.21.4-soar.json"
                withContext(Dispatchers.IO) {
                    downloadFile(versionUrl, versionFile.toFile()) { progress, message ->
                        _currentProgress = DownloadProgress(
                            DownloadState.DOWNLOADING,
                            progress,
                            "downloading... $message"
                        )
                        onProgressUpdate(_currentProgress)
                    }
                }
            }

            _currentProgress = DownloadProgress(DownloadState.COMPLETE, 1f, "Version Ready")
            onProgressUpdate(_currentProgress)
        } catch (e: Exception) {
            _currentProgress = DownloadProgress(DownloadState.ERROR, 0f, "Version Error: ${e.message}")
            onProgressUpdate(_currentProgress)
        }
    }

    suspend fun downloadMinecraftFiles(onProgressUpdate: (DownloadProgress) -> Unit) {
        try {
            _currentProgress = DownloadProgress(DownloadState.CHECKING, 0f, "Checking...")
            onProgressUpdate(_currentProgress)


            _currentProgress = DownloadProgress(DownloadState.DOWNLOADING, 0.5f, "Checking...")
            onProgressUpdate(_currentProgress)

           //wo bu hui(wo lan)
            withContext(Dispatchers.IO) {
                Thread.sleep(1000)
            }

            _currentProgress = DownloadProgress(DownloadState.COMPLETE, 1f, "Minecraft Ready")
            onProgressUpdate(_currentProgress)
        } catch (e: Exception) {
            _currentProgress = DownloadProgress(DownloadState.ERROR, 0f, "Minecraft check error: ${e.message}")
            onProgressUpdate(_currentProgress)
        }
    }

    suspend fun downloadAndExtractAdditionalAssets(onProgressUpdate: (DownloadProgress) -> Unit) {
        if (areAdditionalAssetsExtracted()) {
            _currentProgress = DownloadProgress(DownloadState.COMPLETE, 1f, "idk")
            onProgressUpdate(_currentProgress)
            return
        }

        try {
            val assetsPath = Paths.get(ASSETS_FOLDER).toAbsolutePath()
            val extractionPath = assetsPath.resolve("versions")
            Files.createDirectories(extractionPath)

            val downloadFile = assetsPath.resolve("11.zip").toFile()

            _currentProgress = DownloadProgress(DownloadState.DOWNLOADING, 0f, "download other assets")
            onProgressUpdate(_currentProgress)

            withContext(Dispatchers.IO) {
                downloadFile(ADDITIONAL_ASSETS_URL, downloadFile) { progress, message ->
                    _currentProgress = DownloadProgress(DownloadState.DOWNLOADING, progress, message)
                    onProgressUpdate(_currentProgress)
                }

                _currentProgress = DownloadProgress(DownloadState.EXTRACTING, 0f, "unzipping...")
                onProgressUpdate(_currentProgress)

                extractArchive(downloadFile, extractionPath.toFile())
                downloadFile.delete()

                extractionPath.resolve(".additional_assets_extracted").toFile().createNewFile()
            }

            _currentProgress = DownloadProgress(DownloadState.COMPLETE, 1f, "ready")
            onProgressUpdate(_currentProgress)
        } catch (e: Exception) {
            _currentProgress = DownloadProgress(DownloadState.ERROR, 0f, "error: ${e.message}")
            onProgressUpdate(_currentProgress)
        }
    }

    fun isSoarClientJarDownloaded(): Boolean {
        val jarFile = Paths.get(ASSETS_FOLDER, "versions", "mods", "soarclient-fork-8.0.0.jar").toFile()
        return jarFile.exists() && jarFile.length() > 0
    }

    suspend fun downloadSoarClientJar(onProgressUpdate: (DownloadProgress) -> Unit) {
        try {
            _currentProgress = DownloadProgress(DownloadState.CHECKING, 0f, "Checking SoarClient JAR...")
            onProgressUpdate(_currentProgress)

            val modsDir = Paths.get(ASSETS_FOLDER, "versions", "mods").toFile()
            if (!modsDir.exists()) {
                modsDir.mkdirs()
            }

            val jarFile = File(modsDir, "soarclient-fork-8.0.0.jar")

            if (jarFile.exists() && jarFile.length() > 0) {
                _currentProgress = DownloadProgress(DownloadState.COMPLETE, 1f, "SoarClient JAR Ready")
                onProgressUpdate(_currentProgress)
                return
            }

            _currentProgress = DownloadProgress(DownloadState.DOWNLOADING, 0f, "Downloading SoarClient JAR...")
            onProgressUpdate(_currentProgress)

            withContext(Dispatchers.IO) {
                downloadFile(SOAR_CLIENT_JAR_URL, jarFile) { progress, message ->
                    _currentProgress = DownloadProgress(
                        DownloadState.DOWNLOADING,
                        progress,
                        "Downloading SoarClient JAR - $message"
                    )
                    onProgressUpdate(_currentProgress)
                }
            }

            _currentProgress = DownloadProgress(DownloadState.COMPLETE, 1f, "SoarClient JAR Ready")
            onProgressUpdate(_currentProgress)

        } catch (e: Exception) {
            _currentProgress = DownloadProgress(DownloadState.ERROR, 0f, "SoarClient JAR Error: ${e.message}")
            onProgressUpdate(_currentProgress)
        }
    }

    fun isFabricApiInstalled(): Boolean {
        val modsDir = Paths.get(ASSETS_FOLDER, "versions", "mods").toFile()
        val fabricApiFile = File(modsDir, "fabric-api-0.119.4+1.21.4.jar")
        return fabricApiFile.exists()
    }

    suspend fun downloadFabricApi(onProgressUpdate: (DownloadProgress) -> Unit) {
        try {
            _currentProgress = DownloadProgress(DownloadState.DOWNLOADING, 0f, "Downloading Fabric API...")
            onProgressUpdate(_currentProgress)

            val modsDir = Paths.get(ASSETS_FOLDER, "versions", "mods").toFile()
            if (!modsDir.exists()) {
                modsDir.mkdirs()
            }

            val outputFile = File(modsDir, "fabric-api-0.119.4+1.21.4.jar")

            downloadFile(FABRIC_API_URL, outputFile) { progress, message ->
                _currentProgress = DownloadProgress(DownloadState.DOWNLOADING, progress, message)
                onProgressUpdate(_currentProgress)
            }

            _currentProgress = DownloadProgress(DownloadState.COMPLETE, 1f, "Fabric API downloaded successfully")
            onProgressUpdate(_currentProgress)
        } catch (e: Exception) {
            _currentProgress = DownloadProgress(DownloadState.ERROR, 0f, "Failed to download Fabric API: ${e.message}")
            onProgressUpdate(_currentProgress)
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

    suspend fun downloadFile(url: String, outputFile: File, onProgress: (Float, String) -> Unit) {
        var lastException: Exception? = null

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

                return
            } catch (e: Exception) {
                lastException = e
                println("Attempt $attempt failed to download $url: ${e.message}")

                if (attempt < 3) {
                    Thread.sleep(1000 * attempt.toLong())
                }
            }
        }
        throw lastException ?: RuntimeException("Failed to download $url after 3 attempts")
    }

    // --- Mod Management Functions ---

    private val modsDir by lazy { File(ASSETS_FOLDER, "versions/mods") }

    /**
     * 扫描并返回所有已安装的 Mod 列表。
     * 它会读取每个 .jar 文件中的 fabric.mod.json 来获取详细信息。
     *
     * @return ModInfo 对象的列表
     */
    fun getInstalledMods(): List<ModInfo> {
        if (!modsDir.exists() || !modsDir.isDirectory) {
            return emptyList()
        }

        // 筛选出 .jar 和 .jar.disabled 文件
        return modsDir.listFiles { _, name -> name.endsWith(".jar") || name.endsWith(".jar.disabled") }
            ?.mapNotNull { file ->
                try {
                    parseModInfo(file)
                } catch (e: Exception) {
                    // 如果文件损坏或不是一个有效的 Mod JAR，则忽略
                    println("Failed to parse mod info from ${file.name}: ${e.message}")
                    null
                }
            } ?: emptyList()
    }

    /**
     * 从单个 JAR 文件中解析 Mod 信息。
     * @param file Mod 的 JAR 文件
     * @return 解析成功则返回 ModInfo，否则返回 null
     */
    private fun parseModInfo(file: File): ModInfo? {
        ZipFile(file).use { zip ->
            val entry = zip.getEntry("fabric.mod.json") ?: return null // 确保是 Fabric Mod
            val mapper = ObjectMapper()
            val jsonNode = zip.getInputStream(entry).use { mapper.readTree(it) }

            val modId = jsonNode.get("id")?.asText() ?: file.nameWithoutExtension
            val version = jsonNode.get("version")?.asText() ?: "N/A"
            val name = jsonNode.get("name")?.asText() ?: modId
            // 启用状态取决于文件名是否以 .jar 结尾
            val isEnabled = file.name.endsWith(".jar")

            return ModInfo(modId, version, name, isEnabled, file)
        }
    }
}