package com.banglalink.toffee.data.repository.impl

import com.banglalink.toffee.data.database.dao.SubscriptionInfoDao
import com.banglalink.toffee.data.database.entities.SubscriptionInfo
import com.banglalink.toffee.data.repository.SubscriptionInfoRepository

class SubscriptionInfoRepositoryImpl(private val dao: SubscriptionInfoDao): SubscriptionInfoRepository {
    override suspend fun insert(subscriptionInfo: SubscriptionInfo): Long {
        return dao.insert(subscriptionInfo)
    }

    override suspend fun delete(subscriptionInfo: SubscriptionInfo): Int {
        return dao.delete(subscriptionInfo)
    }

    override suspend fun getAllSubscription(): List<SubscriptionInfo> {
        return dao.getAllSubscription()
    }

    override suspend fun updateSubscription(status: Int, channelId: Int, subscriberId: Int): Int {
        return dao.updateSubscription(status, channelId, subscriberId)
    }

    override suspend fun insertAll(vararg subscriptionInfoList: SubscriptionInfo): LongArray {
        return dao.insertAll(*subscriptionInfoList)
    }
}