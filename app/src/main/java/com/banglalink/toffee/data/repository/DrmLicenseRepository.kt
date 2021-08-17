package com.banglalink.toffee.data.repository

import com.banglalink.toffee.data.database.entities.DrmLicenseEntity

interface DrmLicenseRepository {
    suspend fun insert(item: DrmLicenseEntity)
    suspend fun delete(item: DrmLicenseEntity)
    suspend fun getByChannelId(channelId: Long): DrmLicenseEntity?
}