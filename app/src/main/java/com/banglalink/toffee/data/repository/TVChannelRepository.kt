package com.banglalink.toffee.data.repository

import com.banglalink.toffee.data.database.entities.TVChannelItem
import kotlinx.coroutines.flow.Flow

interface TVChannelRepository {
    suspend fun insertNewItems(vararg items: TVChannelItem)
    suspend fun insertRecentItems(item: TVChannelItem)
    fun getAllItems(): Flow<List<TVChannelItem>>
}