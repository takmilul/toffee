package com.banglalink.toffee.data.repository.impl

import androidx.paging.PagingSource
import com.banglalink.toffee.data.database.dao.HistoryItemDao
import com.banglalink.toffee.data.database.entities.HistoryItem
import com.banglalink.toffee.data.repository.HistoryRepository

class HistoryRepositoryImpl (private val dao: HistoryItemDao): HistoryRepository {
    override suspend fun insert(item: HistoryItem): Long {
        return dao.insert(item)
    }

    override suspend fun delete(item: HistoryItem) {
        dao.delete(item)
    }

    override suspend fun update(item: HistoryItem) {
        dao.update(item)
    }

    override suspend fun deleteAll() {
        dao.deleteAll()
    }

    override fun getAllItems(): PagingSource<Int, HistoryItem> {
        return dao.getHistoryItems()
    }
}