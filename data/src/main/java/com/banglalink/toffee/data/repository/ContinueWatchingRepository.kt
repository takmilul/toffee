package com.banglalink.toffee.data.repository

import com.banglalink.toffee.data.database.entities.ContinueWatchingItem
import kotlinx.coroutines.flow.Flow

interface ContinueWatchingRepository {
    suspend fun insertItem(item: ContinueWatchingItem)
    fun getAllItemsByCategory(catId: Int): Flow<List<ContinueWatchingItem>>
    suspend fun deleteByContentId(customerId: Int, contentId: Long)
}
