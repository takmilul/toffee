package com.banglalink.toffee.data.database.dao

import androidx.room.*
import com.banglalink.toffee.data.database.entities.SubscriptionInfo

@Dao
interface SubscriptionInfoDao {
    @Insert
    suspend fun insert(subscriptionInfo: SubscriptionInfo): Long

    @Delete
    suspend fun delete(subscriptionInfo: SubscriptionInfo): Int

    @Query("SELECT * FROM SubscriptionInfo")
    suspend fun getAllSubscription(): List<SubscriptionInfo>

    @Query("UPDATE SubscriptionInfo SET status = :status WHERE channelId == :channelId AND subscriberId == :subscriberId")
    suspend fun updateSubscription(status: Int, channelId: Int, subscriberId: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg SubscriptionInfoList: SubscriptionInfo): LongArray

}