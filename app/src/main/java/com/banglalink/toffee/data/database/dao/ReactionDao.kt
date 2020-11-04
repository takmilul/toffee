package com.banglalink.toffee.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.banglalink.toffee.data.database.entities.ReactionInfo

@Dao
interface ReactionDao {
    @Insert
    suspend fun insert(reactionInfo: ReactionInfo)
    
    @Delete
    suspend fun delete(reactionInfo: ReactionInfo)
    
    @Query("SELECT * FROM ReactionInfo")
    suspend fun getAllReaction(): List<ReactionInfo>
    
    @Query("SELECT * FROM ReactionInfo WHERE customerId == :customerId AND contentId == :contentId")
    suspend fun getReactionByContentId(customerId: Int, contentId: String): ReactionInfo?
}