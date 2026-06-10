package com.repoappstore.di

import com.repoappstore.data.api.GitHubApiService
import com.repoappstore.data.auth.GitHubAuthService
import com.repoappstore.data.database.AppDao
import com.repoappstore.data.database.DownloadDao
import com.repoappstore.data.database.RepositoryDao
import com.repoappstore.domain.repository.GitHubRepository
import com.repoappstore.domain.repository.AuthRepository
import com.repoappstore.domain.repository.DownloadRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAuthRepository(
        apiService: GitHubApiService,
        authService: GitHubAuthService
    ): AuthRepository {
        return AuthRepository(apiService, authService)
    }

    @Provides
    @Singleton
    fun provideGitHubRepository(
        apiService: GitHubApiService,
        authService: GitHubAuthService,
        repositoryDao: RepositoryDao,
        appDao: AppDao
    ): GitHubRepository {
        return GitHubRepository(apiService, authService, repositoryDao, appDao)
    }

    @Provides
    @Singleton
    fun provideDownloadRepository(
        downloadDao: DownloadDao,
        appDao: AppDao
    ): DownloadRepository {
        return DownloadRepository(downloadDao, appDao)
    }
}