package dev.eatgrapes.soarlauncher.utils

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.File

@JsonIgnoreProperties(ignoreUnknown = true)
data class LocalModInfo(
    @JsonProperty("schemaVersion")
    val schemaVersion: Int = 1,
    
    @JsonProperty("id")
    val id: String = "",
    
    @JsonProperty("version")
    val version: String = "",
    
    @JsonProperty("name")
    val name: String = "",
    
    @JsonProperty("description")
    val description: String = "",
    
    @JsonProperty("authors")
    val authors: List<String> = emptyList(),
    
    @JsonProperty("contact")
    val contact: Map<String, String> = emptyMap(),
    
    @JsonProperty("icon")
    val icon: String? = null,
    
    @JsonProperty("environment")
    val environment: String = "*",
    
    @JsonProperty("mixins")
    val mixins: List<String>? = null,
    
    @JsonProperty("depends")
    val depends: Map<String, String> = emptyMap(),
    
    @JsonProperty("provides")
    val provides: List<String> = emptyList(),
    
    @JsonProperty("license")
    val license: String = ""
) {
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    lateinit var jarFile: File
    
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    var isInstalled: Boolean = false
}