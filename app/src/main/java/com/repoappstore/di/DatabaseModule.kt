package com.repoappstore.di

import android.content.Context
import com.repoappstore.data.database.RepoAppStoreDatabase
import com.repoappstore.data.database.AppDao
import com.repoappstore.data.database.DownloadDao
import com.repoappstore.data.database.RepositoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): RepoAppStoreDatabase {
        return RepoAppStoreDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideRepositoryDao(db: RepoAppStoreDatabase): RepositoryDao {
        return db.repositoryDao()
    }

    @Provides
    @Singleton
    fun provideAppDao(db: RepoAppStoreDatabase): AppDao {
        return db.appDao()
    }

    @Provides
    @Singleton
    fun provideDownloadDao(db: RepoAppStoreDatabase): DownloadDao {
        return db.downloadDao()
    }
}