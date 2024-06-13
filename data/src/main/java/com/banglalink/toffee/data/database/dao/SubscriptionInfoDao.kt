package com.banglalink.toffee.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.banglalink.toffee.data.database.entities.SubscriptionInfo

@Dao
interface SubscriptionInfoDao {
    @Insert
    suspend fun insert(subscriptionInfo: SubscriptionInfo): Long

    @Delete
    suspend fun delete(subscriptionInfo: SubscriptionInfo): Int

    @Query("SELECT * FROM SubscriptionInfo")
    suspend fun getAllSubscription(): List<SubscriptionInfo>

    @Query("SELECT * FROM SubscriptionInfo WHERE channelId == :channelId AND customerId == :subscriberId LIMIT 1")
    suspend fun getSubscriptionInfoByChannelId(channelId: Int, subscriberId: Int): SubscriptionInfo?
    
    @Query("DELETE FROM SubscriptionInfo WHERE channelId == :channelId AND customerId == :subscriberId")
    suspend fun deleteSubscription(channelId: Int, subscriberId: Int): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg SubscriptionInfoList: SubscriptionInfo): LongArray
}