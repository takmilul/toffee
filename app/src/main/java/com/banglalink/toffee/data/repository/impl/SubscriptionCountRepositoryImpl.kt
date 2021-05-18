package com.banglalink.toffee.data.repository.impl

import androidx.room.withTransaction
import com.banglalink.toffee.data.database.ToffeeDatabase
import com.banglalink.toffee.data.database.dao.SubscriptionCountDao
import com.banglalink.toffee.data.database.entities.SubscriptionCount
import com.banglalink.toffee.data.repository.SubscriptionCountRepository

class SubscriptionCountRepositoryImpl(private val db: ToffeeDatabase, private val dao: SubscriptionCountDao): SubscriptionCountRepository {
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

    override suspend fun updateSubscriptionCount(subscriptionStatusList: ArrayList<SubscriptionCount>) {
        db.withTransaction {
            val dbList = mutableListOf<SubscriptionCount>()
            val map: MutableMap<Int, SubscriptionCount> = mutableMapOf()
            subscriptionStatusList.forEach {
                val item = map[it.channelId]
                if (item == null){
                    map[it.channelId] = it
                }
                else {
                    item.status++
                }
            }
            val ids = map.keys.toList()
            for (id in ids.indices step 999) {
                val subList = ids.subList(id, minOf(ids.size, id + 999))
                val dbSubscriptionCounts = dao.getSubscriptionListByContentIds(subList)
                dbList.addAll(dbSubscriptionCounts)
            }
            val updateList = dbList.map { item ->
                item.status = map[item.channelId]?.status?.plus(item.status)?.takeIf { it > 0 } ?: 0
                map.remove(item.channelId)
                item
            }.toMutableList()
            updateList.addAll(map.values)
            dao.insertAll(*updateList.toTypedArray())
        }
    }
    
    override suspend fun updateSubscriptionCount(channelId: Int, status: Int): Int {
        val count = dao.getSubscriberCount(channelId)

        return if(count == null){
            status.takeIf { it > 0 }?.run { 
                dao.insert(SubscriptionCount(channelId, this.toLong())).toInt()
            } ?: 0
        }
        else{
            if(status == 1) {
                dao.updateSubscription(channelId, count + status)
            }
            else{
                count.takeIf { it > 0 }?.run {
                    dao.updateSubscription(channelId, this + status)
                } ?: 0
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