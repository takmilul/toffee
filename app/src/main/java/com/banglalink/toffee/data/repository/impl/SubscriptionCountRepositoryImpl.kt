package com.banglalink.toffee.data.repository.impl

import com.banglalink.toffee.data.database.dao.SubscriptionCountDao
import com.banglalink.toffee.data.database.entities.SubscriptionCount
import com.banglalink.toffee.data.repository.SubscriptionCountRepository

class SubscriptionCountRepositoryImpl(private val dao: SubscriptionCountDao): SubscriptionCountRepository {
    override suspend fun insert(subscriptionCount: SubscriptionCount): Long {
        return dao.insert(subscriptionCount)
    }

    override suspend fun delete(subscriptionCount: SubscriptionCount): Int {
        return dao.delete(subscriptionCount)
    }

    override suspend fun getAllSubscription(): List<SubscriptionCount> {
        return dao.getAllSubscription()
    }

    override suspend fun updateSubscription(status: Int, channelId: Int, subscriberId: Int): Int {
        return dao.updateSubscription(status, channelId, subscriberId)
    }

    override suspend fun insertAll(vararg subscriptionCountList: SubscriptionCount): LongArray {
        return dao.insertAll(*subscriptionCountList)
    }
}