package dev.eatgrapes.soarlauncher.launch

import dev.eatgrapes.soarlauncher.config.ConfigManager
import dev.eatgrapes.soarlauncher.utils.ResourceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.Paths
import java.util.*
import kotlin.random.Random

object GameLauncher {
    private const val ASSETS_FOLDER = "soar_assets"
    
    private var gameProcess: Process? = null
    private var isGameRunning = false

    suspend fun launchGame(onGameLaunched: (() -> Unit)? = null): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                if (isGameRunning) {
                    println("Game is already running")
                    return@withContext false
                }

                val javaPath = ResourceManager.getJavaExecutablePath()
                if (javaPath == null) {
                    println("Java 21 not found")
                    return@withContext false
                }

                val versionPath = Paths.get(ASSETS_FOLDER, "versions").toAbsolutePath()
                val assetsPath = versionPath.resolve("assets")
                val gameJar = versionPath.resolve("1.21.4-soar.jar").toFile()
                val gameJson = versionPath.resolve("1.21.4-soar.json").toFile()
                val libPath = Paths.get(ASSETS_FOLDER, "libs").toAbsolutePath()
                val modsPath = versionPath.resolve("mods")

                if (!gameJar.exists() || !gameJson.exists()) {
                    println("Game files not found")
                    return@withContext false
                }

                val classpath = buildClasspath(libPath.toFile(), gameJar, modsPath.toFile())

                val username = "Player${Random.nextInt(1000, 9999)}"
                val uuid = generateRandomUUID()

                val command = buildLaunchCommand(
                    javaPath = javaPath,
                    classpath = classpath,
                    gameDirectory = versionPath.toString(),
                    assetsDirectory = assetsPath.toString(),
                    username = username,
                    uuid = uuid,
                    memorySize = "${ConfigManager.getRamAllocation()}G"
                )

                println("Launching game with command: ${command.joinToString(" ")}")

                val processBuilder = ProcessBuilder(command)
                processBuilder.directory(versionPath.toFile())

                val gameLogDir = File(versionPath.toFile(), "logs")
                gameLogDir.mkdirs()
                
                val outputLog = File(gameLogDir, "game-output.log")
                val errorLog = File(gameLogDir, "game-error.log")
                
                processBuilder.redirectOutput(ProcessBuilder.Redirect.to(outputLog))
                processBuilder.redirectError(ProcessBuilder.Redirect.to(errorLog))

                val process = processBuilder.start()
                println("Game launched successfully in separate process (PID: ${process.pid()})")
                println("Game output will be logged to: ${outputLog.absolutePath}")
                println("Game errors will be logged to: ${errorLog.absolutePath}")

                gameProcess = process
                isGameRunning = true

                monitorGameProcess(process)

                onGameLaunched?.invoke()

                return@withContext true

            } catch (e: Exception) {
                println("Failed to launch game: ${e.message}")
                e.printStackTrace()
                return@withContext false
            }
        }
    }

    private fun buildClasspath(libDir: File, gameJar: File, modsDir: File): String {
        val classpathEntries = mutableListOf<String>()

        classpathEntries.add(gameJar.absolutePath)

        if (libDir.exists() && libDir.isDirectory) {
            libDir.walkTopDown()
                .filter { it.isFile && it.extension == "jar" }
                .forEach { classpathEntries.add(it.absolutePath) }
        }

        if (modsDir.exists() && modsDir.isDirectory) {
            modsDir.walkTopDown()
                .filter { it.isFile && it.extension == "jar" }
                .forEach { classpathEntries.add(it.absolutePath) }
        }

        return classpathEntries.joinToString(File.pathSeparator)
    }

    private fun buildLaunchCommand(
        javaPath: String,
        classpath: String,
        gameDirectory: String,
        assetsDirectory: String,
        username: String,
        uuid: String,
        memorySize: String
    ): List<String> {
        val command = mutableListOf(
            javaPath,
            "-Xmx$memorySize",
            "-Xms1G",
            "-XX:+UseG1GC",
            "-XX:+UnlockExperimentalVMOptions",
            "-XX:G1NewSizePercent=20",
            "-XX:G1ReservePercent=20",
            "-XX:MaxGCPauseMillis=50",
            "-XX:G1HeapRegionSize=32M",
            "-Djava.library.path=$gameDirectory/1.21.4-soar-natives",
            "-Djna.tmpdir=$gameDirectory/1.21.4-soar-natives",
            "-Dorg.lwjgl.system.SharedLibraryExtractPath=$gameDirectory/1.21.4-soar-natives",
            "-Dio.netty.native.workdir=$gameDirectory/1.21.4-soar-natives",
            "-Dminecraft.launcher.brand=SoarLauncher",
            "-Dminecraft.launcher.version=1.0",
            "-Dfml.modLoadingWorkerCount=2"
        )
        
        command.addAll(listOf(
            "-cp", classpath,
            "net.fabricmc.loader.impl.launch.knot.KnotClient",
            "--username", username,
            "--version", "1.21.4-soar",
            "--gameDir", gameDirectory,
            "--assetsDir", "assets",
            "--assetIndex", "19",
            "--userType", "legacy",
            "--versionType", "release",
            "--accessToken", "0",
            "--uuid", uuid
        ))
        
        return command
    }

    private fun generateRandomUUID(): String {
        return "${Random.nextInt(16).toString(16)}${Random.nextInt(16).toString(16)}${Random.nextInt(16).toString(16)}${Random.nextInt(16).toString(16)}${Random.nextInt(16).toString(16)}${Random.nextInt(16).toString(16)}${Random.nextInt(16).toString(16)}${Random.nextInt(16).toString(16)}-" +
                "${Random.nextInt(16).toString(16)}${Random.nextInt(16).toString(16)}${Random.nextInt(16).toString(16)}${Random.nextInt(16).toString(16)}-" +
                "${Random.nextInt(16).toString(16)}${Random.nextInt(16).toString(16)}${Random.nextInt(16).toString(16)}${Random.nextInt(16).toString(16)}-" +
                "${Random.nextInt(16).toString(16)}${Random.nextInt(16).toString(16)}${Random.nextInt(16).toString(16)}${Random.nextInt(16).toString(16)}-" +
                "${Random.nextInt(16).toString(16)}${Random.nextInt(16).toString(16)}${Random.nextInt(16).toString(16)}${Random.nextInt(16).toString(16)}${Random.nextInt(16).toString(16)}${Random.nextInt(16).toString(16)}${Random.nextInt(16).toString(16)}${Random.nextInt(16).toString(16)}${Random.nextInt(16).toString(16)}${Random.nextInt(16).toString(16)}${Random.nextInt(16).toString(16)}${Random.nextInt(16).toString(16)}"
    }

    private fun monitorGameProcess(process: Process) {
        Thread {
            try {
                val exitCode = process.waitFor()
                isGameRunning = false
                gameProcess = null
                println("Game process has ended with exit code: $exitCode")
            } catch (e: InterruptedException) {
                isGameRunning = false
                gameProcess = null
                println("Game process monitoring was interrupted: ${e.message}")
            }
        }.start()
    }

    fun isGameCurrentlyRunning(): Boolean {
        return isGameRunning
    }
    
    fun getGameProcessId(): Long? {
        return gameProcess?.pid()
    }
}