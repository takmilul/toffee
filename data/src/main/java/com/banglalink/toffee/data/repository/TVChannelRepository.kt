package com.banglalink.toffee.data.repository

import androidx.paging.PagingSource
import com.banglalink.toffee.data.database.entities.TVChannelItem
import kotlinx.coroutines.flow.Flow

interface TVChannelRepository {
    suspend fun insertNewItems(vararg items: TVChannelItem)
    suspend fun deleteItems(item: TVChannelItem)
    suspend fun deleteAllRecentItems()
    suspend fun insertRecentItems(item: TVChannelItem)
    suspend fun getRecentItemById(channelId: Long, isStingray: Int,isFmRadio:Int): TVChannelItem?
    suspend fun updateRecentItemPayload(channelId: Long, isStingray: Int, isFm: Int, viewCount: Long, payload: String)
    fun getAllItems(): Flow<List<TVChannelItem>?>
    fun getStingrayItems(): Flow<List<TVChannelItem>?>
    fun getFmItems(): Flow<List<TVChannelItem>?>
    fun getRecentItemsFlow(): Flow<List<TVChannelItem>?>
    suspend fun getNonStingrayRecentItems(): List<TVChannelItem>?
    fun getStingrayRecentItems(): Flow<List<TVChannelItem>?>
    fun getAllChannels(isStingray: Boolean): PagingSource<Int, String>
}