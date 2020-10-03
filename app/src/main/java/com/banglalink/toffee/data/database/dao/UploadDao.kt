package com.banglalink.toffee.data.database.dao

import androidx.room.*
import com.banglalink.toffee.data.database.entities.UploadInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface UploadDao {
    @Insert
    suspend fun insert(uploadInfo: UploadInfo)

    @Update
    suspend fun update(uploadInfo: UploadInfo)

    @Delete
    suspend fun delete(uploadInfo: UploadInfo)

    @Query("SELECT * FROM UploadInfo")
    fun getUploads(): Flow<List<UploadInfo>>

    @Query("SELECT * from UploadInfo WHERE uploadId=:uploadId")
    suspend fun getUploadById(uploadId: Long): UploadInfo?

    @Query("UPDATE UploadInfo SET completedPercent=:completed, completedPercent=:percent WHERE uploadId=:uploadId")
    suspend fun updateProgressById(uploadId: Long, completed: Long, percent: Int)
}
