package com.banglalink.toffee.data.repository

import com.banglalink.toffee.data.database.entities.SubscriptionInfo

interface SubscriptionInfoRepository {
    suspend fun insert(subscriptionInfo: SubscriptionInfo): Long
    suspend fun delete(subscriptionInfo: SubscriptionInfo): Int
    suspend fun getAllSubscriptionInfo(): List<SubscriptionInfo>
    suspend fun getSubscriptionInfoByChannelId(channelId: Int, customerId: Int): SubscriptionInfo?
    suspend fun deleteSubscriptionInfo(channelId: Int, subscriberId: Int): Int
    suspend fun insertAll(vararg subscriptionInfoList: SubscriptionInfo): LongArray
}