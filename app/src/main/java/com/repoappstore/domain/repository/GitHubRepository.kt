package com.repoappstore.domain.repository

import com.repoappstore.data.api.GitHubApiService
import com.repoappstore.data.auth.GitHubAuthService
import com.repoappstore.data.database.AppDao
import com.repoappstore.data.database.RepositoryDao
import com.repoappstore.data.model.AppEntity
import com.repoappstore.data.model.AppUI
import com.repoappstore.data.model.GitHubRelease
import com.repoappstore.data.model.GitHubRepository
import com.repoappstore.data.model.Repository
import com.repoappstore.data.model.toUI
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.time.LocalDateTime
import javax.inject.Inject

class GitHubRepository @Inject constructor(
    private val apiService: GitHubApiService,
    private val authService: GitHubAuthService,
    private val repositoryDao: RepositoryDao,
    private val appDao: AppDao
) {

    // ========== Repositories ==========
    fun getAllRepositories(): Flow<List<Repository>> {
        return repositoryDao.getAllActive()
    }

    fun getUserGitHubRepositories(): Flow<Result<List<GitHubRepository>>> = flow {
        try {
            val auth = authService.getAuthHeader()
                ?: throw Exception("Belum login, silakan login terlebih dahulu")
            val repos = apiService.getUserRepositories(auth = auth)
            emit(Result.success(repos))
        } catch (e: Exception) {
            Timber.e(e, "Gagal memuat repository user")
            emit(Result.failure(e))
        }
    }

    suspend fun addRepository(owner: String, repoName: String): Result<Repository> {
        return try {
            val auth = authService.getAuthHeader()
            val gitHubRepo = apiService.getRepository(
                owner = owner,
                repo = repoName,
                auth = auth
            )
            val repository = Repository(
                name = gitHubRepo.name,
                owner = gitHubRepo.owner.login,
                url = gitHubRepo.html_url,
                description = gitHubRepo.description ?: "",
                addedDate = LocalDateTime.now().toString()
            )
            val id = repositoryDao.insert(repository)
            Result.success(repository.copy(id = id))
        } catch (e: Exception) {
            Timber.e(e, "Gagal menambah repository")
            Result.failure(e)
        }
    }

    suspend fun deleteRepository(repository: Repository) {
        repositoryDao.delete(repository)
        appDao.deleteByRepository(repository.id)
    }

    // ========== Releases ==========
    fun getReleases(
        owner: String,
        repoName: String
    ): Flow<Result<List<GitHubRelease>>> = flow {
        try {
            val auth = authService.getAuthHeader()
            val releases = apiService.getReleases(
                owner = owner,
                repo = repoName,
                auth = auth
            )
            emit(Result.success(releases))
        } catch (e: Exception) {
            Timber.e(e, "Gagal memuat releases")
            emit(Result.failure(e))
        }
    }

    suspend fun syncRepositoryReleases(repository: Repository): Result<Int> {
        return try {
            val auth = authService.getAuthHeader()
            val releases = apiService.getReleases(
                owner = repository.owner,
                repo = repository.name,
                auth = auth
            )
            val apps = releases
                .filter { !it.draft }
                .map { release ->
                    val asset = release.assets.firstOrNull()
                    AppEntity(
                        repositoryId = repository.id,
                        repositoryName = repository.name,
                        name = release.name ?: release.tag_name,
                        version = release.tag_name,
                        description = release.body ?: "",
                        releaseDate = release.published_at,
                        author = release.author.login,
                        authorAvatar = release.author.avatar_url,
                        downloadUrl = asset?.browser_download_url ?: "",
                        downloadCount = asset?.download_count ?: 0,
                        fileSize = asset?.size ?: 0L,
                        htmlUrl = release.html_url,
                        lastUpdated = LocalDateTime.now().toString()
                    )
                }
            appDao.insertAll(apps)
            repositoryDao.updateLastSync(repository.id, LocalDateTime.now().toString())
            Result.success(apps.size)
        } catch (e: Exception) {
            Timber.e(e, "Gagal sync repository")
            Result.failure(e)
        }
    }

    // ========== Apps ==========
    fun getAppsByRepository(repositoryId: Long): Flow<List<AppUI>> {
        return appDao.getByRepository(repositoryId).map { list ->
            list.map { it.toUI() }
        }
    }

    fun searchApps(keyword: String): Flow<List<AppUI>> {
        return appDao.search(keyword).map { list ->
            list.map { it.toUI() }
        }
    }

    fun getInstalledApps(): Flow<List<AppUI>> {
        return appDao.getInstalled().map { list ->
            list.map { it.toUI() }
        }
    }

    suspend fun getAppById(appId: Long): AppUI? {
        return appDao.getById(appId)?.toUI()
    }

    // ========== README ==========
    fun getReadme(owner: String, repoName: String): Flow<Result<String>> = flow {
        try {
            val auth = authService.getAuthHeader()
            val readme = apiService.getReadme(
                owner = owner,
                repo = repoName,
                auth = auth
            )
            emit(Result.success(readme))
        } catch (e: Exception) {
            Timber.e(e, "Gagal memuat README")
            emit(Result.failure(Exception("README tidak tersedia untuk repository ini")))
        }
    }

    // ========== Search GitHub ==========
    fun searchGitHubRepositories(query: String): Flow<Result<List<GitHubRepository>>> = flow {
        try {
            val auth = authService.getAuthHeader()
            val result = apiService.searchRepositories(
                query = query,
                auth = auth
            )
            emit(Result.success(result.items))
        } catch (e: Exception) {
            Timber.e(e, "Gagal mencari repository di GitHub")
            emit(Result.failure(e))
        }
    }
}