package com.banglalink.toffee.data.database.dao

import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.room.*
import com.banglalink.toffee.data.database.entities.TVChannelItem
import kotlinx.coroutines.flow.Flow

@Dao
abstract class TVChannelDao {
    @Insert
    abstract suspend fun insert(vararg items: TVChannelItem)

    @Update
    abstract suspend fun update(item: TVChannelItem)

    @Query("SELECT * FROM TVChannelItem ORDER BY priority, updateTime DESC")
    abstract fun getAllItems(): Flow<List<TVChannelItem>>

    @Query("SELECT * FROM TVChannelItem WHERE categoryName NOT IN (\"Recent\") ORDER BY priority")
    abstract fun getAllChannels(): PagingSource<Int, TVChannelItem>

    @Query("DELETE FROM TVChannelItem WHERE categoryName NOT IN (\"Recent\")")
    abstract suspend fun deleteAll()

    @Query("SELECT * FROM TVChannelItem WHERE categoryName=\"Recent\"")
    abstract suspend fun getRecentItems(): List<TVChannelItem>

    @Query("DELETE FROM TVChannelItem where categoryName=\"Recent\" AND id NOT IN " +
            "(SELECT id from TVChannelItem WHERE categoryName=\"Recent\" ORDER BY updateTime DESC LIMIT 5)")
    abstract suspend fun deleteExtraRecents()

    @Transaction
    open suspend fun insertRecentItem(item: TVChannelItem) {
        val recItem = getRecentItems().find { it.channelId == item.channelId }
        recItem?.let {
            recItem.updateTime = System.currentTimeMillis()
            update(recItem)
        } ?: insert(item)
        deleteExtraRecents()
    }

    @Transaction
    open suspend fun insertNewItems(vararg items: TVChannelItem) {
        deleteAll()
        insert(*items)
    }

    @Query("SELECT * FROM TVChannelItem where categoryName=\"Movies\" ORDER BY viewCount DESC")
    abstract fun getPopularMovieChannels(): PagingSource<Int, TVChannelItem>
}