package com.repoappstore.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.repoappstore.data.model.AppEntity
import com.repoappstore.data.model.DownloadEntity
import com.repoappstore.data.model.Repository

@Database(
    entities = [
        Repository::class,
        AppEntity::class,
        DownloadEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class RepoAppStoreDatabase : RoomDatabase() {
    abstract fun repositoryDao(): RepositoryDao
    abstract fun appDao(): AppDao
    abstract fun downloadDao(): DownloadDao

    companion object {
        @Volatile
        private var INSTANCE: RepoAppStoreDatabase? = null

        fun getInstance(context: Context): RepoAppStoreDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RepoAppStoreDatabase::class.java,
                    "repo_appstore_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}