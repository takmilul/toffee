package com.banglalink.toffee.data.repository.impl

import com.banglalink.toffee.data.database.dao.ContinueWatchingDao
import com.banglalink.toffee.data.database.entities.ContinueWatchingItem
import com.banglalink.toffee.data.repository.ContinueWatchingRepository
import com.banglalink.toffee.data.storage.Preference
import kotlinx.coroutines.flow.Flow

class ContinueWatchingRepositoryImpl(
    private val dao: ContinueWatchingDao,
    private val pref: Preference)
: ContinueWatchingRepository {
    override suspend fun insertItem(item: ContinueWatchingItem) {
        dao.insertItem(item)
    }

    override fun getAllItemsByCategory(catId: Int): Flow<List<ContinueWatchingItem>> {
        return dao.getAllItemsByCategory(catId, pref.customerId)
    }
}