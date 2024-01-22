package com.banglalink.toffee.data.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.banglalink.toffee.data.database.entities.HistoryItem

@Dao
interface HistoryItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: HistoryItem): Long

    @Update
    suspend fun update(item: HistoryItem)

    @Delete
    suspend fun delete(item: HistoryItem)

    @Query("DELETE FROM HistoryItem")
    suspend fun deleteAll()

    @Query("SELECT * from HistoryItem ORDER BY id DESC")
    fun getHistoryItems(): PagingSource<Int, HistoryItem>
}