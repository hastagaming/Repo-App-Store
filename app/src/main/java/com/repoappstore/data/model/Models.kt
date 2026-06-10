package com.repoappstore.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

// ========== Repository ==========
@Entity(tableName = "repositories")
@Serializable
data class Repository(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val owner: String,
    val url: String,
    val description: String = "",
    val isActive: Boolean = true,
    val addedDate: String = LocalDateTime.now().toString(),
    val lastSync: String? = null
)

// ========== GitHub API Response ==========
@Serializable
data class GitHubUser(
    val login: String,
    val id: Long,
    val avatar_url: String,
    val name: String? = null,
    val bio: String? = null,
    val public_repos: Int = 0,
    val followers: Int = 0
)

@Serializable
data class GitHubRepository(
    val id: Long,
    val name: String,
    val owner: GitHubOwner,
    val description: String? = null,
    val html_url: String,
    val homepage: String? = null,
    val language: String? = null,
    val stargazers_count: Int = 0,
    val watchers_count: Int = 0,
    val forks_count: Int = 0,
    val open_issues_count: Int = 0,
    val updated_at: String,
    val pushed_at: String? = null
)

@Serializable
data class GitHubOwner(
    val login: String,
    val avatar_url: String,
    val html_url: String
)

@Serializable
data class GitHubRelease(
    val id: Long,
    val name: String?,
    val tag_name: String,
    val body: String? = null,
    val draft: Boolean = false,
    val prerelease: Boolean = false,
    val published_at: String,
    val author: GitHubOwner,
    val assets: List<GitHubAsset> = emptyList(),
    val html_url: String
)

@Serializable
data class GitHubAsset(
    val id: Long,
    val name: String,
    val size: Long,
    val download_count: Int,
    val browser_download_url: String,
    val created_at: String,
    val updated_at: String,
    @SerialName("content_type")
    val contentType: String
)

@Serializable
data class GitHubReadmeResponse(
    val name: String,
    val path: String,
    val sha: String,
    val size: Int,
    val type: String,
    val content: String, // base64 encoded
    val encoding: String,
    val url: String,
    val html_url: String,
    val git_url: String,
    val download_url: String? = null
)

// ========== App Entity (Database) ==========
@Entity(tableName = "applications")
data class AppEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val repositoryId: Long,
    val repositoryName: String,
    val name: String,
    val version: String,
    val description: String = "",
    val releaseDate: String = "",
    val author: String = "",
    val authorAvatar: String = "",
    val downloadUrl: String = "",
    val downloadCount: Int = 0,
    val fileSize: Long = 0,
    val isInstalled: Boolean = false,
    val installedVersion: String? = null,
    val htmlUrl: String = "",
    val lastUpdated: String = LocalDateTime.now().toString()
)

data class AppUI(
    val id: Long,
    val repositoryName: String,
    val name: String,
    val version: String,
    val description: String,
    val releaseDate: String,
    val author: String,
    val authorAvatar: String,
    val downloadUrl: String,
    val downloadCount: Int,
    val fileSize: Long,
    val isInstalled: Boolean,
    val installedVersion: String?,
    val htmlUrl: String
)

fun AppEntity.toUI(): AppUI = AppUI(
    id = id,
    repositoryName = repositoryName,
    name = name,
    version = version,
    description = description,
    releaseDate = releaseDate,
    author = author,
    authorAvatar = authorAvatar,
    downloadUrl = downloadUrl,
    downloadCount = downloadCount,
    fileSize = fileSize,
    isInstalled = isInstalled,
    installedVersion = installedVersion,
    htmlUrl = htmlUrl
)

// ========== Download State ==========
sealed class DownloadState {
    object Idle : DownloadState()
    object Queued : DownloadState()
    data class Downloading(val progress: Int) : DownloadState()
    object Downloaded : DownloadState()
    data class Error(val message: String) : DownloadState()
    object Installing : DownloadState()
    object Installed : DownloadState()
}

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val appId: Long,
    val appName: String,
    val repositoryName: String,
    val downloadUrl: String,
    val filePath: String = "",
    val fileSize: Long,
    val state: String = "Idle",
    val progress: Int = 0,
    val startTime: String = LocalDateTime.now().toString(),
    val completedTime: String? = null,
    val errorMessage: String? = null
)

// ========== Auth ==========
@Serializable
data class GitHubTokenResponse(
    val access_token: String,
    val token_type: String,
    val scope: String
)

data class AuthUser(
    val login: String,
    val id: Long,
    val avatarUrl: String,
    val name: String?,
    val bio: String?,
    val publicRepos: Int,
    val followers: Int,
    val token: String,
    val tokenType: String
)

// ========== UI State ==========
data class HomeUiState(
    val repositories: List<Repository> = emptyList(),
    val applications: List<AppUI> = emptyList(),
    val filteredApplications: List<AppUI> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val selectedRepositoryId: Long? = null,
    val isSyncing: Boolean = false
)

data class RepoDetailUiState(
    val repository: Repository? = null,
    val releases: List<GitHubRelease> = emptyList(),
    val readme: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

data class AuthUiState(
    val isLoggedIn: Boolean = false,
    val user: AuthUser? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val oauthUrl: String? = null,
    val oauthState: String? = null
)

data class DownloadUiState(
    val downloads: List<DownloadEntity> = emptyList(),
    val activeDownloads: List<DownloadEntity> = emptyList(),
    val isLoading: Boolean = false
)