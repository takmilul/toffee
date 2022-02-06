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
    
    override fun getStingrayItems(): Flow<List<TVChannelItem>> {
        return dao.getStingrayItems()
    }
    
    override fun getRecentItems(): Flow<List<TVChannelItem>> {
        return dao.getRecentItemsFlow()
    }
    
    override fun getStingrayRecentItems(): Flow<List<TVChannelItem>> {
        return dao.getStingrayRecentItemsFlow()
    }
    
    override fun getAllChannels(isStingray: Boolean): PagingSource<Int, TVChannelItem> {
        return if (isStingray) dao.getStingrayChannels() else dao.getAllChannels()
    }

    override fun getPopularMovieChannels(): PagingSource<Int, TVChannelItem> {
        return dao.getPopularMovieChannels()
    }
    
    override suspend fun getPopularMovieChannelsCount(): Int {
        return dao.getPopularMovieChannelsCount()
    }
}