package com.banglalink.toffee.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.banglalink.toffee.data.database.entities.ReactionInfo

@Dao
interface ReactionDao {
    @Insert
    suspend fun insert(reactionInfo: ReactionInfo): Long
    
    @Delete
    suspend fun delete(reactionInfo: ReactionInfo): Int
    
    @Query("SELECT * FROM ReactionInfo")
    suspend fun getAllReaction(): List<ReactionInfo>
    
    @Query("SELECT * FROM ReactionInfo WHERE customerId == :customerId AND contentId == :contentId")
    suspend fun getReactionByContentId(customerId: Int, contentId: Long): ReactionInfo?
    
    @Query("UPDATE ReactionInfo SET reactionType = :reaction WHERE customerId == :customerId AND contentId == :contentId")
    suspend fun updateReactionByContentId(customerId: Int, contentId: Long, reaction: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg reactionInfoList: ReactionInfo): LongArray
    
    @Query("DELETE FROM ReactionInfo WHERE customerId == :customerId AND contentId == :contentId")
    suspend fun deleteByContentId(customerId: Int, contentId: Long)
}