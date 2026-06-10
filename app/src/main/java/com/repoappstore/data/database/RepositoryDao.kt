package com.repoappstore.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.repoappstore.data.model.Repository
import kotlinx.coroutines.flow.Flow

@Dao
interface RepositoryDao {
    @Query("SELECT * FROM repositories WHERE isActive = 1 ORDER BY addedDate DESC")
    fun getAllActive(): Flow<List<Repository>>

    @Query("SELECT * FROM repositories WHERE id = :id")
    suspend fun getById(id: Long): Repository?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(repository: Repository): Long

    @Update
    suspend fun update(repository: Repository)

    @Delete
    suspend fun delete(repository: Repository)

    @Query("UPDATE repositories SET isActive = 0 WHERE id = :id")
    suspend fun deactivate(id: Long)

    @Query("UPDATE repositories SET lastSync = :lastSync WHERE id = :id")
    suspend fun updateLastSync(id: Long, lastSync: String)
}