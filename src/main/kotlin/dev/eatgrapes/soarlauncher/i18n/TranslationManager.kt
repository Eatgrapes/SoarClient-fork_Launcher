package dev.eatgrapes.soarlauncher.i18n

import java.io.File
import java.util.*

object TranslationManager {
    private const val CONFIG_FILE = "config.properties"
    private const val LANGUAGE_KEY = "language"
    
    private var currentLanguage: String = Language.DEFAULT
    private var translations: MutableMap<String, String> = mutableMapOf()
    
    init {
        loadLanguage(getSavedLanguage())
    }
    
    fun getAvailableLanguages(): List<String> {
        try {
            val resources = javaClass.classLoader.getResources("soar/lang")
            val languages = mutableListOf<String>()
            
            while (resources.hasMoreElements()) {
                val url = resources.nextElement()
                if (url.protocol == "file") {
                    val langDir = File(url.file)
                    langDir.listFiles()
                        ?.filter { it.extension == "lang" }
                        ?.mapTo(languages) { it.nameWithoutExtension }
                } else if (url.protocol == "jar") {
                    val jarConnection = url.openConnection()
                    val jarFile = (jarConnection as? java.net.JarURLConnection)?.jarFile
                    jarFile?.entries()?.toList()
                        ?.filter { it.name.startsWith("soar/lang/") && it.name.endsWith(".lang") }
                        ?.mapTo(languages) { 
                            it.name.substringAfterLast("/").substringBeforeLast(".lang") 
                        }
                }
            }
            
            return languages.distinct().sorted()
        } catch (e: Exception) {
            println("Error getting available languages: ${e.message}")
            return listOf(Language.ENGLISH, Language.CHINESE_SIMPLIFIED)
        }
    }
    
    fun loadLanguage(languageCode: String) {
        try {
            val resourcePath = "soar/lang/$languageCode.lang"
            val inputStream = javaClass.classLoader.getResourceAsStream(resourcePath)
                ?: throw IllegalArgumentException("Language file not found: $resourcePath")
            
            val newTranslations = mutableMapOf<String, String>()
            inputStream.bufferedReader().use { reader ->
                reader.lines().forEach { line ->
                    if (line.isNotBlank() && !line.startsWith("#")) {
                        val parts = line.split("=", limit = 2)
                        if (parts.size == 2) {
                            newTranslations[parts[0].trim()] = parts[1].trim()
                        }
                    }
                }
            }
            
            translations.clear()
            translations.putAll(newTranslations)
            currentLanguage = languageCode
            saveLanguage(languageCode)
            
        } catch (e: Exception) {
            println("Failed to load language '$languageCode': ${e.message}")
            if (languageCode != Language.DEFAULT) {
                loadLanguage(Language.DEFAULT)
            }
        }
    }
    
    fun getCurrentLanguage(): String = currentLanguage
    
    fun getTranslation(key: String, default: String = key): String {
        return translations[key] ?: default
    }
    
    private fun getSavedLanguage(): String {
        return try {
            val configFile = File(CONFIG_FILE)
            if (configFile.exists()) {
                val props = Properties()
                configFile.inputStream().use { props.load(it) }
                props.getProperty(LANGUAGE_KEY, Language.DEFAULT)
            } else {
                Language.DEFAULT
            }
        } catch (e: Exception) {
            Language.DEFAULT
        }
    }
    
    private fun saveLanguage(languageCode: String) {
        try {
            val props = Properties()
            val configFile = File(CONFIG_FILE)
            
            if (configFile.exists()) {
                configFile.inputStream().use { props.load(it) }
            }
            
            props.setProperty(LANGUAGE_KEY, languageCode)
            configFile.outputStream().use { props.store(it, "Soar Launcher Configuration") }
        } catch (e: Exception) {
            println("Failed to save language preference: ${e.message}")
        }
    }
}