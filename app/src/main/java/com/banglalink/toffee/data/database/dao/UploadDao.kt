package com.banglalink.toffee.data.database.dao

import androidx.room.*
import com.banglalink.toffee.data.database.entities.UploadInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface UploadDao {
    @Insert
    suspend fun insert(uploadInfo: UploadInfo): Long

    @Update
    suspend fun update(uploadInfo: UploadInfo)

    @Delete
    suspend fun delete(uploadInfo: UploadInfo)

    @Query("DELETE FROM UploadInfo")
    suspend fun deleteAll()

    @Query("SELECT * FROM UploadInfo")
    suspend fun getUploads(): List<UploadInfo>

    @Query("SELECT * FROM UploadInfo WHERE status in (0, 1, 2, 5) ORDER BY uploadId DESC")
    fun getActiveUploads(): Flow<List<UploadInfo>>

    @Query("SELECT * FROM UploadInfo WHERE status in (0, 1, 2, 5) ORDER BY uploadId DESC")
    suspend fun getActiveUploadsList(): List<UploadInfo>

    @Query("SELECT * from UploadInfo WHERE uploadId=:uploadId")
    suspend fun getUploadById(uploadId: Long): UploadInfo?

    @Query("UPDATE UploadInfo SET completedPercent=:completed, completedPercent=:percent, fileSize=:totalSize WHERE uploadId=:uploadId")
    suspend fun updateProgressById(uploadId: Long, completed: Long, percent: Int, totalSize: Long)
}
