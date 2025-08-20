package dev.eatgrapes.soarlauncher.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object ModrinthAPI {
    private const val BASE_URL = "https://api.modrinth.com/v2"
    private val mapper: ObjectMapper = jacksonObjectMapper()
    
    fun searchMods(query: String = "", offset: Int = 0): ModrinthSearchResult? {
        try {
            val facets = "[[\"categories:fabric\"],[\"versions:1.21.4\"],[\"project_type:mod\"]]"
            val encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString())
            val encodedFacets = URLEncoder.encode(facets, StandardCharsets.UTF_8.toString())
            
            val url = "https://api.modrinth.com/v2/search?limit=20&offset=$offset&query=$encodedQuery&facets=$encodedFacets"
            
            val connection = URI.create(url).toURL().openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "SoarClient-fork_Launcher/1.0")
            connection.setRequestProperty("Accept", "application/json")
            
            val responseCode = connection.responseCode
            if (responseCode == 200) {
                val responseBody = connection.inputStream.bufferedReader().readText()
                val mapper = ObjectMapper()
                return mapper.readValue(responseBody, ModrinthSearchResult::class.java)
            } else {
                println("Modrinth API request failed with code: $responseCode")
                return null
            }
        } catch (e: Exception) {
            println("Failed to search mods: ${e.message}")
            e.printStackTrace()
            return null
        }
    }
    
    suspend fun getProject(projectId: String): ModrinthProjectDetails? {
        return withContext(Dispatchers.IO) {
            try {
                val url = "$BASE_URL/project/$projectId"
                val result = makeRequest(url)
                if (result != null) {
                    mapper.readValue<ModrinthProjectDetails>(result)
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
    
    suspend fun getProjectVersions(projectId: String): List<ModrinthVersion> {
        return withContext(Dispatchers.IO) {
            try {
                val url = "$BASE_URL/project/$projectId/version"
                val result = makeRequest(url)
                if (result != null) {
                    mapper.readValue<List<ModrinthVersion>>(result)
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }
    
    private fun makeRequest(url: String): String? {
        try {
            val connection = URI.create(url).toURL().openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "SoarClient-fork_Launcher/1.0")
            connection.setRequestProperty("Accept", "application/json")
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            
            val responseCode = connection.responseCode
            if (responseCode == 200) {
                return connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                println("Modrinth API request failed with code: $responseCode")
                return null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}