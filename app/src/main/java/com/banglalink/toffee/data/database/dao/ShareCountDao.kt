package com.banglalink.toffee.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.banglalink.toffee.data.database.entities.ShareCount

@Dao
interface ShareCountDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ShareCount): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg items: ShareCount): LongArray

    @Query("SELECT count FROM ShareCount WHERE contentId = :contentId LIMIT 1")
    suspend fun getShareCountByContentId(contentId: Int): Long?
    
    @Query("UPDATE ShareCount SET count = :count WHERE contentId = :contentId")
    suspend fun updateShareCount(contentId: Int, count: Long): Int
}
