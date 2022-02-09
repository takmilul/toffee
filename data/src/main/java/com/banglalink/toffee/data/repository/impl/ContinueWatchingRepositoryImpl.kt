package com.banglalink.toffee.data.repository.impl

import com.banglalink.toffee.data.database.dao.ContinueWatchingDao
import com.banglalink.toffee.data.database.entities.ContinueWatchingItem
import com.banglalink.toffee.data.repository.ContinueWatchingRepository
import com.banglalink.toffee.data.storage.SessionPreference
import kotlinx.coroutines.flow.Flow

class ContinueWatchingRepositoryImpl(
    private val dao: ContinueWatchingDao,
    private val pref: SessionPreference)
: ContinueWatchingRepository {
    
    override suspend fun insertItem(item: ContinueWatchingItem) {
        dao.insertItem(item)
    }

    override fun getAllItemsByCategory(catId: Int): Flow<List<ContinueWatchingItem>> {
        return dao.getAllItemsByCategory(catId, pref.customerId)
    }

    override suspend fun deleteByContentId(customerId: Int, contentId: Long) = dao.deleteByContentId(customerId, contentId)
}