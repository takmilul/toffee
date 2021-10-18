package com.banglalink.toffee.data.repository

import com.banglalink.toffee.data.database.entities.SubscriptionCount

interface SubscriptionCountRepository {
    suspend fun insert(subscriptionCount: SubscriptionCount): Long
    suspend fun insertAll(vararg subscriptionCountList: SubscriptionCount): LongArray
    suspend fun delete(subscriptionCount: SubscriptionCount): Int
    suspend fun getAllSubscriptionCount(): List<SubscriptionCount>
    suspend fun getSubscriberCount(channelId: Int): Long
    suspend fun getSubscriptionCount(channelId: Int): SubscriptionCount
    suspend fun updateSubscriptionCount(subscriptionStatusList: ArrayList<SubscriptionCount>)
    suspend fun updateSubscriptionCount(channelId: Int, status: Int): Int
}