package com.banglalink.toffee.data.database.dao

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
    
    @Delete
    abstract suspend fun delete(item: TVChannelItem)
    
    @Query("SELECT * FROM TVChannelItem WHERE isStingray != 1 AND categoryName NOT IN (\"Recent\") ORDER BY priority, updateTime DESC")
    abstract fun getAllItems(): Flow<List<TVChannelItem>?>
    
    @Query("SELECT * FROM TVChannelItem WHERE isStingray == 1 AND categoryName NOT IN (\"Recent\") ORDER BY priority, updateTime DESC")
    abstract fun getStingrayItems(): Flow<List<TVChannelItem>?>

    @Query("SELECT * FROM TVChannelItem WHERE isFmRadio == 1 AND categoryName NOT IN (\"Recent\") ORDER BY priority, updateTime DESC")
    abstract fun getFmItems(): Flow<List<TVChannelItem>?>
    
    @Query("SELECT payload FROM TVChannelItem WHERE isStingray != 1 AND categoryName NOT IN (\"Recent\") ORDER BY priority")
    abstract fun getAllChannels(): PagingSource<Int, String>
    
    @Query("SELECT payload FROM TVChannelItem WHERE isStingray == 1 AND categoryName NOT IN (\"Recent\") ORDER BY priority")
    abstract fun getStingrayChannels(): PagingSource<Int, String>
    
    @Query("DELETE FROM TVChannelItem WHERE isStingray != 1 AND categoryName NOT IN (\"Recent\")")
    abstract suspend fun deleteAllTvItems()
    
    @Query("DELETE FROM TVChannelItem WHERE isStingray == 1 AND categoryName NOT IN (\"Recent\")")
    abstract suspend fun deleteAllStingrayItems()
    
    @Query("SELECT * FROM TVChannelItem WHERE categoryName=\"Recent\"")
    abstract suspend fun getRecentItems(): List<TVChannelItem>?
    
    @Query("SELECT * FROM TVChannelItem WHERE categoryName = \"Recent\" AND channelId = :channelId AND isStingray = :isStingray AND isFmRadio = :isFmRadio")
    abstract suspend fun getRecentItemById(channelId: Long, isStingray: Int , isFmRadio: Int): TVChannelItem?
    
    @Query("UPDATE TVChannelItem SET payload = :payload, viewCount = :viewCount WHERE categoryName = \"Recent\" AND channelId = :channelId AND isStingray = :isStingray AND isFmRadio = :isFmRadio")
    abstract suspend fun updateRecentItemPayload(channelId: Long, isStingray: Int, isFmRadio: Int, viewCount: Long, payload: String)
    
    @Query("SELECT * FROM TVChannelItem WHERE isStingray == 1 AND categoryName=\"Recent\"")
    abstract suspend fun getStingrayRecentItems(): List<TVChannelItem>?
    
    @Query("SELECT * FROM TVChannelItem WHERE isStingray != 1 AND categoryName=\"Recent\"")
    abstract suspend fun getNonStingrayRecentItems(): List<TVChannelItem>?
    
    @Query("SELECT * FROM TVChannelItem WHERE isStingray != 1 AND categoryName=\"Recent\" ORDER BY updateTime DESC")
    abstract fun getRecentItemsFlow(): Flow<List<TVChannelItem>?>
    
    @Query("SELECT * FROM TVChannelItem WHERE isStingray == 1 AND isFmRadio == 0 AND categoryName=\"Recent\" ORDER BY updateTime DESC")
    abstract fun getStingrayRecentItemsFlow(): Flow<List<TVChannelItem>?>

    @Query("SELECT * FROM TVChannelItem WHERE isStingray == 0 AND isFmRadio == 1 AND categoryName=\"Recent\" ORDER BY updateTime DESC")
    abstract fun getFmRecentItemsFlow(): Flow<List<TVChannelItem>?>

    @Query("DELETE FROM TVChannelItem WHERE isStingray != 1 AND categoryName=\"Recent\" AND id NOT IN " +
            "(SELECT id from TVChannelItem WHERE isStingray != 1 AND categoryName=\"Recent\" ORDER BY updateTime DESC LIMIT 11)")
    abstract suspend fun deleteExtraRecent()

    @Query("DELETE FROM TVChannelItem WHERE categoryName=\"Recent\"")
    abstract suspend fun deleteAllRecentItems()

    @Query("DELETE FROM TVChannelItem WHERE isStingray == 1 AND categoryName=\"Recent\" AND id NOT IN " +
            "(SELECT id from TVChannelItem WHERE isStingray == 1 AND categoryName=\"Recent\" ORDER BY updateTime DESC LIMIT 11)")
    abstract suspend fun deleteExtraStingrayRecent()
    
    @Transaction
    open suspend fun insertRecentItem(item: TVChannelItem) {
        val recItem = getRecentItems()?.find { (it.channelId == item.channelId) and (it.isStingray == item.isStingray) and (it.isFmRadio == item.isFmRadio) }
        recItem?.let {
            recItem.updateTime = System.currentTimeMillis()
            update(recItem)
        } ?: insert(item)
        if (item.isStingray) {
            deleteExtraStingrayRecent()
        } else {
            deleteExtraRecent()
        }
    }
    
    @Transaction
    open suspend fun insertNewItems(vararg items: TVChannelItem) {
        if (items.isNotEmpty()) {
            if (items.first().isStingray) {
                deleteAllStingrayItems()
            } else {
                deleteAllTvItems()
            }
        }
        insert(*items)
    }
    
    @Query("SELECT channelId FROM TVChannelItem WHERE isStingray != 1 AND categoryName NOT IN (\"Recent\") ORDER BY priority, updateTime DESC")
    abstract fun getAllChannelList(): List<Long>?
}