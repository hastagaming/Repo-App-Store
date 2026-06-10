package com.repoappstore.di

import android.content.Context
import com.repoappstore.data.auth.GitHubAuthService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideGitHubAuthService(
        @ApplicationContext context: Context
    ): GitHubAuthService {
        return GitHubAuthService(context)
    }
}