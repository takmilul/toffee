package com.banglalink.toffee.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.banglalink.toffee.data.database.entities.ReactionStatusItem

@Dao
interface ReactionStatusDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reactionStatusItem: ReactionStatusItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg items: ReactionStatusItem): LongArray

    @Query("SELECT * FROM ReactionStatusItem WHERE contentId = :contentId")
    suspend fun getReactionStatusByChannelId(contentId: Long): List<ReactionStatusItem>?
    
    @Query("SELECT reactionCount FROM ReactionStatusItem WHERE contentId = :contentId AND reactionType = :reactionType")
    suspend fun getReactionCountByReactionType(contentId: Long, reactionType: Int): Long?
    
    @Query("UPDATE ReactionStatusItem SET reactionCount = :count, updateTime = :updateTime WHERE contentId = :contentId AND reactionType = :reactionType")
    suspend fun updateReactionStatusByChannelId(contentId: Long, reactionType: Int, updateTime: Long, count: Long): Int
    
    @Query("SELECT * FROM ReactionStatusItem WHERE contentId in (:contentIds)")
    suspend fun getReactionListByChannelIds(contentIds: List<Int>): List<ReactionStatusItem>
}
