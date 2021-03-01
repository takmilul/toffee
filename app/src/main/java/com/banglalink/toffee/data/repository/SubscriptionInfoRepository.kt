package com.banglalink.toffee.data.repository

import com.banglalink.toffee.data.database.entities.SubscriptionInfo

interface SubscriptionInfoRepository {
    suspend fun insert(subscriptionInfo: SubscriptionInfo): Long
    suspend fun delete(subscriptionInfo: SubscriptionInfo): Int
    suspend fun getAllSubscription(): List<SubscriptionInfo>
    suspend fun updateSubscription(status: Int, channelId: Int, subscriberId: Int): Int
    suspend fun insertAll(vararg subscriptionInfoList: SubscriptionInfo): LongArray
}