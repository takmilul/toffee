package com.banglalink.toffee.data.repository.impl

import android.util.Log
import androidx.room.withTransaction
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.data.database.ToffeeDatabase
import com.banglalink.toffee.data.database.dao.PlayerEventsDao
import com.banglalink.toffee.data.database.entities.PlayerEventData
import com.banglalink.toffee.data.repository.PlayerEventRepository
import com.banglalink.toffee.notification.PLAYER_EVENTS_TOPIC
import com.banglalink.toffee.notification.PubSubMessageUtil

class PlayerEventRepositoryImpl(
    private val db: ToffeeDatabase,
    private val dao: PlayerEventsDao
): PlayerEventRepository {
    
    override suspend fun insert(item: PlayerEventData): Long {
        return try {
            dao.insert(item)
        } catch (e: Exception) {
            0
        }
    }
    
    override suspend fun insertAll(vararg items: PlayerEventData): LongArray {
        return try {
            dao.insertAll(*items)
        } catch (e: Exception) {
            longArrayOf(0)
        }
    }
    
    override suspend fun sendTopEventToPubSubAndRemove() {
        try {
            db.withTransaction {
                dao.getTopEventData()?.onEach {
                    PubSubMessageUtil.sendMessage(it, PLAYER_EVENTS_TOPIC)
                }?.size?.also {
                    if (it > 0) {
                        dao.deleteTopEventData(it)
                    }
                }
            }
        } catch (e: Exception) {
            Log.i("PLAYER_EVENT", "sendTopEventToPubSubAndRemove: failed. errorMsg: ${e.message}")
            ToffeeAnalytics.logBreadCrumb("Failed to send player event pubsub. errorMsg: ${e.message}")
        }
    }
    
    override suspend fun sendAllRemainingEventToPubSubAndRemove() {
        try {
            db.withTransaction {
                dao.getAllRemainingEventData()?.onEach {
                    PubSubMessageUtil.sendMessage(it, PLAYER_EVENTS_TOPIC)
                }?.size?.also {
                    if (it > 0) {
                        dao.deleteTopEventData(it)
                    }
                }
            }
        } catch (e: Exception) {
            Log.i("PLAYER_EVENT", "sendTopEventToPubSubAndRemove: failed")
            ToffeeAnalytics.logBreadCrumb("Failed to send player event pubsub")
        }
    }
}