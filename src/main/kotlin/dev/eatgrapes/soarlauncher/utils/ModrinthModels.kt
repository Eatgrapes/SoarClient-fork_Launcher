package dev.eatgrapes.soarlauncher.utils

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class ModrinthSearchResult(
    @JsonProperty("hits")
    val hits: List<ModrinthProject> = emptyList(),
    @JsonProperty("offset")
    val offset: Int = 0,
    @JsonProperty("limit")
    val limit: Int = 0,
    @JsonProperty("total_hits")
    val totalHits: Int = 0
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ModrinthProject(
    @JsonProperty("project_id")
    val projectId: String = "",
    @JsonProperty("slug")
    val slug: String = "",
    @JsonProperty("title")
    val title: String = "",
    @JsonProperty("description")
    val description: String = "",
    @JsonProperty("categories")
    val categories: List<String> = emptyList(),
    @JsonProperty("versions")
    val versions: List<String> = emptyList(),
    @JsonProperty("downloads")
    val downloads: Int = 0,
    @JsonProperty("icon_url")
    val iconUrl: String? = null,
    @JsonProperty("author")
    val author: String = "",
    @JsonProperty("created_timestamp")
    val createdTimestamp: Long = 0,
    @JsonProperty("latest_version")
    val latestVersion: String = ""
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ModrinthProjectDetails(
    @JsonProperty("id")
    val id: String = "",
    @JsonProperty("slug")
    val slug: String = "",
    @JsonProperty("title")
    val title: String = "",
    @JsonProperty("description")
    val description: String = "",
    @JsonProperty("categories")
    val categories: List<String> = emptyList(),
    @JsonProperty("client_side")
    val clientSide: String = "",
    @JsonProperty("server_side")
    val serverSide: String = "",
    @JsonProperty("body")
    val body: String = "",
    @JsonProperty("project_type")
    val projectType: String = "",
    @JsonProperty("downloads")
    val downloads: Int = 0,
    @JsonProperty("icon_url")
    val iconUrl: String? = null,
    @JsonProperty("color")
    val color: Int? = null,
    @JsonProperty("published")
    val published: String = "",
    @JsonProperty("updated")
    val updated: String = "",
    @JsonProperty("followers")
    val followers: Int = 0,
    @JsonProperty("versions")
    val versions: List<String> = emptyList(),
    @JsonProperty("game_versions")
    val gameVersions: List<String> = emptyList(),
    @JsonProperty("loaders")
    val loaders: List<String> = emptyList()
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ModrinthVersion(
    @JsonProperty("id")
    val id: String = "",
    @JsonProperty("project_id")
    val projectId: String = "",
    @JsonProperty("author_id")
    val authorId: String = "",
    @JsonProperty("featured")
    val featured: Boolean = false,
    @JsonProperty("name")
    val name: String = "",
    @JsonProperty("version_number")
    val versionNumber: String = "",
    @JsonProperty("changelog")
    val changelog: String = "",
    @JsonProperty("changelog_url")
    val changelogUrl: String? = null,
    @JsonProperty("date_published")
    val datePublished: String = "",
    @JsonProperty("downloads")
    val downloads: Int = 0,
    @JsonProperty("version_type")
    val versionType: String = "",
    @JsonProperty("files")
    val files: List<ModrinthFile> = emptyList(),
    @JsonProperty("loaders")
    val loaders: List<String> = emptyList(),
    @JsonProperty("game_versions")
    val gameVersions: List<String> = emptyList()
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ModrinthFile(
    @JsonProperty("hashes")
    val hashes: Map<String, String> = emptyMap(),
    @JsonProperty("url")
    val url: String = "",
    @JsonProperty("filename")
    val filename: String = "",
    @JsonProperty("primary")
    val primary: Boolean = false,
    @JsonProperty("size")
    val size: Long = 0
)