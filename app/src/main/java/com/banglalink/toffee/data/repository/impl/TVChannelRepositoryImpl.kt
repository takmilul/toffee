package com.banglalink.toffee.data.repository.impl

import com.banglalink.toffee.data.database.dao.TVChannelDao
import com.banglalink.toffee.data.database.entities.TVChannelItem
import com.banglalink.toffee.data.repository.TVChannelRepository
import kotlinx.coroutines.flow.Flow

class TVChannelRepositoryImpl(private val dao: TVChannelDao): TVChannelRepository {
    override suspend fun insertNewItems(vararg items: TVChannelItem) {
        dao.insertNewItems(*items)
    }

    override suspend fun insertRecentItems(item: TVChannelItem) {
        dao.insertRecentItem(item)
    }

    override fun getAllItems(): Flow<List<TVChannelItem>> {
        return dao.getAllItems()
    }
}