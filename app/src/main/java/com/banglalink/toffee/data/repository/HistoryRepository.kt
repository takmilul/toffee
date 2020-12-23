package com.banglalink.toffee.data.repository

import androidx.paging.PagingSource
import com.banglalink.toffee.data.database.entities.HistoryItem

interface HistoryRepository {
    suspend fun insert(item: HistoryItem): Long
    suspend fun delete(item: HistoryItem)
    suspend fun update(item: HistoryItem)
    suspend fun deleteAll()
    fun getAllItems(): PagingSource<Int, HistoryItem>
}