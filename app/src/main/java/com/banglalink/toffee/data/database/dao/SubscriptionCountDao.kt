package com.banglalink.toffee.data.database.dao

import androidx.room.*
import com.banglalink.toffee.data.database.entities.SubscriptionCount

@Dao
interface SubscriptionCountDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(subscriptionCount: SubscriptionCount): Long

    @Delete
    suspend fun delete(subscriptionCount: SubscriptionCount): Int

    @Query("SELECT * FROM SubscriptionCount")
    suspend fun getAllSubscription(): List<SubscriptionCount>

    @Query("SELECT status FROM SubscriptionCount WHERE channelId == :channelId")
    suspend fun getSubscriberCount(channelId: Int): Long?

    @Query("SELECT * FROM SubscriptionCount WHERE channelId == :channelId")
    suspend fun getSubscription(channelId: Int): SubscriptionCount
    
    @Query("UPDATE SubscriptionCount SET status = :count WHERE channelId == :channelId")
    suspend fun updateSubscription(channelId: Int, count: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg subscriptionCountList: SubscriptionCount): LongArray

}