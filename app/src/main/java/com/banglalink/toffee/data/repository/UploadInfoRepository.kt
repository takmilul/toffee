package com.banglalink.toffee.data.repository

import com.banglalink.toffee.data.database.entities.UploadInfo
import kotlinx.coroutines.flow.Flow

interface UploadInfoRepository {
    suspend fun insertUploadInfo(item: UploadInfo)
    suspend fun updateUploadInfo(item: UploadInfo)
    suspend fun deleteUploadInfo(item: UploadInfo)
    fun getUploads(): Flow<List<UploadInfo>>
    suspend fun getUploadById(uploadId: Long): UploadInfo?
    suspend fun updateProgressById(uploadId: Long, completedSize: Long, completedPercent: Int)
}