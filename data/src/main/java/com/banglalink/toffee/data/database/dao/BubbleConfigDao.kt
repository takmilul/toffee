package com.banglalink.toffee.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.banglalink.toffee.model.BubbleConfig

@Dao
interface BubbleConfigDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: BubbleConfig): Long
    
    @Query("SELECT * FROM BubbleConfig ORDER BY receiveTime DESC LIMIT 1")
    fun getLatestConfig(): BubbleConfig?
    
    @Query("SELECT * FROM BubbleConfig WHERE id=:id ORDER BY receiveTime DESC LIMIT 1")
    fun getConfigById(id: Long): BubbleConfig?
    
    @Query("DELETE FROM BubbleConfig")
    fun deleteAllRows()
    
    @Transaction
    suspend fun insert(item: BubbleConfig): Long {
        deleteAllRows()
        return insertItem(item)
    }
    
    @Delete
    suspend fun delete(bubbleConfig: BubbleConfig)
}