package com.repoappstore.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.repoappstore.data.model.DownloadEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloads ORDER BY startTime DESC")
    fun getAll(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE appId = :appId ORDER BY startTime DESC LIMIT 1")
    fun getLatest(appId: Long): Flow<DownloadEntity?>

    @Query("SELECT * FROM downloads WHERE state IN ('Queued', 'Downloading')")
    fun getActive(): Flow<List<DownloadEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(download: DownloadEntity): Long

    @Update
    suspend fun update(download: DownloadEntity)

    @Query("UPDATE downloads SET state = :state, progress = :progress WHERE id = :downloadId")
    suspend fun updateProgress(downloadId: Long, state: String, progress: Int)

    @Query("UPDATE downloads SET state = :state, completedTime = :completedTime WHERE id = :downloadId")
    suspend fun markCompleted(downloadId: Long, state: String, completedTime: String)

    @Query("DELETE FROM downloads WHERE id = :downloadId")
    suspend fun delete(downloadId: Long)
}