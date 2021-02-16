package com.banglalink.toffee.data.repository

import androidx.paging.PagingSource
import com.banglalink.toffee.data.database.entities.UserActivities
import com.banglalink.toffee.data.database.entities.ViewCount

interface ViewCountRepository {
    suspend fun insert(item: ViewCount): LongArray
    suspend fun delete(item: ViewCount)
    suspend fun update(item: ViewCount)
    suspend fun getViewCountByChannelId(channelId: Int): Long?
    suspend fun insertAll(vararg viewCount: ViewCount): LongArray
}