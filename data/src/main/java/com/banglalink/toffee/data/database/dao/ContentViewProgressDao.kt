package com.banglalink.toffee.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.banglalink.toffee.data.database.entities.ContentViewProgress

@Dao
interface ContentViewProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contentViewProgress: ContentViewProgress)
    
    @Delete
    suspend fun delete(contentViewProgress: ContentViewProgress)
    
    @Query("SELECT * FROM ContentViewProgress WHERE customerId == :customerId ORDER BY id DESC")
    fun getAllProgress(customerId: Int): List<ContentViewProgress>
    
    @Query("SELECT * FROM ContentViewProgress WHERE customerId == :customerId AND contentId == :contentId ORDER BY id DESC")
    suspend fun getProgressByContent(customerId: Int, contentId: Long): ContentViewProgress?

    @Query("DELETE FROM ContentViewProgress WHERE customerId == :customerId AND contentId == :contentId")
    suspend fun deleteByContentId(customerId: Int, contentId: Long)
}