package com.banglalink.toffee.data.repository.impl

import androidx.paging.PagingSource
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

    override fun getRecentItems(): Flow<List<TVChannelItem>> {
        return dao.getRecentItemsFlow()
    }

    override fun getAllChannels(): PagingSource<Int, TVChannelItem> {
        return dao.getAllChannels()
    }

    override fun getPopularMovieChannels(): PagingSource<Int, TVChannelItem> {
        return dao.getPopularMovieChannels()
    }
}