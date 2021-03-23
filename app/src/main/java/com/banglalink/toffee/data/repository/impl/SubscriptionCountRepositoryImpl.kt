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

    override suspend fun getAllSubscriptionCount(): List<SubscriptionCount> {
        return dao.getAllSubscription()
    }

    override suspend fun getSubscriberCount(channelId: Int): Long {
        return dao.getSubscriberCount(channelId) ?: 0
    }

    override suspend fun updateSubscriptionCount(channelId: Int, status: Int): Int {
        val count = dao.getSubscriberCount(channelId) ?: 0L

        return if(count==0L){
            dao.updateSubscription(channelId, 1)
        }
        else{
            if(status==1) {
                dao.updateSubscription(channelId, count +1)
            }
            else{
                dao.updateSubscription(channelId, count - 1)
            }
        }


    }
    override suspend fun getSubscriptionCount(channelId: Int): SubscriptionCount {
             return dao.getSubscription(channelId)
      }
    override suspend fun insertAll(vararg subscriptionCountList: SubscriptionCount): LongArray {
        return dao.insertAll(*subscriptionCountList)
    }
}