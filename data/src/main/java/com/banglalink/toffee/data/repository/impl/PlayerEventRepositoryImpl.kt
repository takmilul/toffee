package com.banglalink.toffee.data.repository.impl

import androidx.room.withTransaction
import com.banglalink.toffee.data.database.ToffeeDatabase
import com.banglalink.toffee.data.database.dao.PlayerEventsDao
import com.banglalink.toffee.data.database.entities.PlayerEventData
import com.banglalink.toffee.data.repository.PlayerEventRepository
import com.banglalink.toffee.notification.PLAYER_EVENTS_TOPIC
import com.banglalink.toffee.notification.PubSubMessageUtil
import com.google.gson.Gson

class PlayerEventRepositoryImpl(
    private val db: ToffeeDatabase,
    private val dao: PlayerEventsDao
): PlayerEventRepository {
    
    private val gson: Gson = Gson()
    
    override suspend fun insert(item: PlayerEventData): Long {
        return dao.insert(item)
    }
    
    override suspend fun insertAll(vararg items: PlayerEventData): LongArray {
        return dao.insertAll(*items)
    }
    
    override suspend fun sentTopEventToPubsubAndRemove() {
        db.withTransaction {
            val limit = dao.getTopEventData().onEach {
                PubSubMessageUtil.sendMessage(gson.toJson(it), PLAYER_EVENTS_TOPIC)
            }.size
            dao.deleteTopEventData(limit)
        }
    }
}