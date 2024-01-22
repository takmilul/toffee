package com.banglalink.toffee.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.banglalink.toffee.data.database.entities.SubscriptionCount

@Dao
interface SubscriptionCountDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(subscriptionCount: SubscriptionCount): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg subscriptionCountList: SubscriptionCount): LongArray

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

    @Query("SELECT * FROM SubscriptionCount WHERE channelId IN (:contentIds)")
    suspend fun getSubscriptionListByContentIds(contentIds: List<Int>): List<SubscriptionCount>
}