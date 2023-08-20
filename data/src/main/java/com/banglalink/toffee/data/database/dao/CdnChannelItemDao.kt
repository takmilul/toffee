package com.banglalink.toffee.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.banglalink.toffee.data.database.entities.CdnChannelItem

@Dao
interface CdnChannelItemDao {
    @Insert
    suspend fun insert(cdnChannelItem: CdnChannelItem): Long
    
    @Delete
    suspend fun delete(cdnChannelItem: CdnChannelItem): Int
    
    @Update
    suspend fun update(cdnChannelItem: CdnChannelItem)
    
    @Query("SELECT * FROM CdnChannelItem")
    suspend fun getAllCdnChannelItem(): List<CdnChannelItem>
    
    @Query("DELETE FROM CdnChannelItem")
    suspend fun deleteAllCdnChannelItem(): Int
    
    @Query("SELECT * FROM CdnChannelItem WHERE channelId == :channelId ORDER BY id DESC LIMIT 1")
    suspend fun getCdnChannelItemByChannelId(channelId: Long): CdnChannelItem?
    
    @Query("UPDATE CdnChannelItem SET expiryDate = :expiryDate, payload = :payload WHERE channelId = :channelId")
    suspend fun updateCdnChannelItemByChannelId(channelId: Long, expiryDate: String?, payload: String): Int?
    
    @Query("DELETE FROM CdnChannelItem WHERE channelId == :channelId")
    suspend fun deleteCdnChannelItemByChannelId(channelId: Long): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg cdnChannelItemList: CdnChannelItem): LongArray
}