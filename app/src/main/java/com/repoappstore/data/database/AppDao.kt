package com.repoappstore.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.repoappstore.data.model.AppEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM applications WHERE repositoryId = :repositoryId ORDER BY lastUpdated DESC")
    fun getByRepository(repositoryId: Long): Flow<List<AppEntity>>

    @Query("SELECT * FROM applications WHERE id = :appId")
    suspend fun getById(appId: Long): AppEntity?

    @Query("SELECT * FROM applications ORDER BY lastUpdated DESC LIMIT :limit OFFSET :offset")
    fun getPaginated(limit: Int, offset: Int): Flow<List<AppEntity>>

    @Query("SELECT * FROM applications WHERE name LIKE '%' || :keyword || '%' OR description LIKE '%' || :keyword || '%' ORDER BY downloadCount DESC")
    fun search(keyword: String): Flow<List<AppEntity>>

    @Query("SELECT * FROM applications WHERE isInstalled = 1 ORDER BY lastUpdated DESC")
    fun getInstalled(): Flow<List<AppEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(app: AppEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(apps: List<AppEntity>)

    @Update
    suspend fun update(app: AppEntity)

    @Query("UPDATE applications SET isInstalled = 1, installedVersion = :version WHERE id = :appId")
    suspend fun markInstalled(appId: Long, version: String)

    @Query("UPDATE applications SET isInstalled = 0, installedVersion = NULL WHERE id = :appId")
    suspend fun markUninstalled(appId: Long)

    @Query("DELETE FROM applications WHERE repositoryId = :repositoryId")
    suspend fun deleteByRepository(repositoryId: Long)
}