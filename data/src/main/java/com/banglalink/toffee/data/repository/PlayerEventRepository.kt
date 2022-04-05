package com.banglalink.toffee.data.repository

import com.banglalink.toffee.data.database.entities.PlayerEventData

interface PlayerEventRepository {
    suspend fun insert(item: PlayerEventData): Long
    suspend fun insertAll(vararg items: PlayerEventData): LongArray
    suspend fun sendTopEventToPubsubAndRemove()
}