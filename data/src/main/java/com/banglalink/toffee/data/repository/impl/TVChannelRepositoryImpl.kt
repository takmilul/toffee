package com.banglalink.toffee.data.repository.impl

import androidx.paging.PagingSource
import com.banglalink.toffee.data.database.ToffeeDatabase
import com.banglalink.toffee.data.database.dao.TVChannelDao
import com.banglalink.toffee.data.database.entities.TVChannelItem
import com.banglalink.toffee.data.repository.TVChannelRepository
import kotlinx.coroutines.flow.Flow

class TVChannelRepositoryImpl(private val db: ToffeeDatabase, private val dao: TVChannelDao): TVChannelRepository {
    override suspend fun insertNewItems(vararg items: TVChannelItem) {
        dao.insertNewItems(*items)
    }
    
    override suspend fun deleteItems(item: TVChannelItem) {
        return dao.delete(item)
    }
    
    override suspend fun deleteAllRecentItems() {
        return dao.deleteAllRecentItems()
    }
    
    override suspend fun insertRecentItems(item: TVChannelItem) {
        dao.insertRecentItem(item)
    }



    override suspend fun getRecentItemById(channelId: Long, isStingray: Int, isFmRadio: Int): TVChannelItem? {
        return dao.getRecentItemById(channelId, isStingray,isFmRadio)
    }


    override suspend fun updateRecentItemPayload(channelId: Long, isStingray: Int, isFm: Int, viewCount: Long, payload: String) {
        dao.updateRecentItemPayload(channelId, isStingray,isFm, viewCount, payload)
    }
    
    override fun getAllItems(): Flow<List<TVChannelItem>?> {
        return dao.getAllItems()
    }
    
    override fun getStingrayItems(): Flow<List<TVChannelItem>?> {
        return dao.getStingrayItems()
    }

    override fun getFmItems(): Flow<List<TVChannelItem>?> {
        return dao.getFmItems()
    }

    override fun getRecentItemsFlow(): Flow<List<TVChannelItem>?> {
        return dao.getRecentItemsFlow()
    }
    
    override suspend fun getNonStingrayRecentItems(): List<TVChannelItem>? {
        return dao.getNonStingrayRecentItems()
    }
    
    override fun getStingrayRecentItems(): Flow<List<TVChannelItem>?> {
        return dao.getStingrayRecentItemsFlow()
    }

    override fun getFmRecentItems(): Flow<List<TVChannelItem>?> {
        return dao.getFmRecentItemsFlow()
    }

    override fun getAllChannels(isStingray: Boolean): PagingSource<Int, String> {
        return if (isStingray) dao.getStingrayChannels() else dao.getAllChannels()
    }
}