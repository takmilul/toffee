package com.banglalink.toffee.data.repository.impl

import com.banglalink.toffee.data.database.dao.UploadDao
import com.banglalink.toffee.data.database.entities.UploadInfo
import com.banglalink.toffee.data.repository.UploadInfoRepository
import kotlinx.coroutines.flow.Flow

class UploadInfoRepositoryImpl(private val uploadDao: UploadDao): UploadInfoRepository {
    override suspend fun insertUploadInfo(item: UploadInfo): Long {
        return uploadDao.insert(item)
    }

    override suspend fun updateUploadInfo(item: UploadInfo) {
        uploadDao.update(item)
    }

    override suspend fun deleteUploadInfo(item: UploadInfo) {
        uploadDao.delete(item)
    }

    override suspend fun deleteAll() {
        uploadDao.deleteAll()
    }

    override fun getUploads(): Flow<List<UploadInfo>> {
        return uploadDao.getUploads()
    }

    override fun getActiveUploads(): Flow<List<UploadInfo>> {
        return uploadDao.getActiveUploads()
    }

    override suspend fun getUploadById(uploadId: Long): UploadInfo? {
        return uploadDao.getUploadById(uploadId)
    }

    override suspend fun updateProgressById(
        uploadId: Long,
        completedSize: Long,
        completedPercent: Int,
        totalSize: Long
    ) {
        uploadDao.updateProgressById(uploadId, completedSize, completedPercent, totalSize)
    }
}