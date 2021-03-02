package com.banglalink.toffee.data.database.dao

import androidx.room.*
import com.banglalink.toffee.data.database.entities.SubscriptionCount

@Dao
interface SubscriptionCountDao {
    @Insert
    suspend fun insert(subscriptionCount: SubscriptionCount): Long

    @Delete
    suspend fun delete(subscriptionCount: SubscriptionCount): Int

    @Query("SELECT * FROM subscription_count")
    suspend fun getAllSubscription(): List<SubscriptionCount>

    @Query("UPDATE subscription_count SET status = :status WHERE channelId == :channelId AND subscriberId == :subscriberId")
    suspend fun updateSubscription(status: Int, channelId: Int, subscriberId: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg subscriptionCountList: SubscriptionCount): LongArray

}