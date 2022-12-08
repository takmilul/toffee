package com.banglalink.toffee.data.database.dao

import androidx.room.*
import com.banglalink.toffee.data.database.entities.SubscribedItem

@Dao
interface SubscribedItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: SubscribedItem): Long

    @Delete
    suspend fun delete(item: SubscribedItem)

    @Query("DELETE FROM SubscribedItem")
    suspend fun deleteAll()

    @Query("SELECT isFavorite from SubscribedItem WHERE channelId=:channelId")
    suspend fun isSubscribed(channelId: Long): Int?
}