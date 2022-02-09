package com.banglalink.toffee.data.database.dao

import androidx.room.*
import com.banglalink.toffee.data.database.entities.DrmLicenseEntity

@Dao
interface DrmLicenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: DrmLicenseEntity): Long

    @Delete
    suspend fun delete(item: DrmLicenseEntity)

    @Query("DELETE FROM DrmLicenseEntity WHERE channelId=:channelId")
    suspend fun deleteByChannelId(channelId: Long)

    @Query("DELETE FROM DrmLicenseEntity")
    suspend fun deleteAll()

    @Query("SELECT * FROM DrmLicenseEntity WHERE channelId=:channelId")
    suspend fun getByChannelId(channelId: Long): DrmLicenseEntity?

    @Query("SELECT * FROM DrmLicenseEntity WHERE contentId=:contentId")
    suspend fun getByContentId(contentId: String): DrmLicenseEntity?
}