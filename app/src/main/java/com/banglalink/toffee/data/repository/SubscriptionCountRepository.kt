package com.banglalink.toffee.data.repository

import com.banglalink.toffee.data.database.entities.SubscriptionCount

interface SubscriptionCountRepository {
    suspend fun insert(subscriptionCount: SubscriptionCount): Long
    suspend fun delete(subscriptionCount: SubscriptionCount): Int
    suspend fun getAllSubscription(): List<SubscriptionCount>
    suspend fun updateSubscription(status: Int, channelId: Int, subscriberId: Int): Int
    suspend fun insertAll(vararg subscriptionCountList: SubscriptionCount): LongArray
}