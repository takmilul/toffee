package com.banglalink.toffee.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.banglalink.toffee.data.database.entities.ReactionStatusItem

@Dao
interface ReactionStatusDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg items: ReactionStatusItem): LongArray

    @Query("SELECT * FROM reaction_status_item WHERE channel_id = :channelId")
    suspend fun getReactionStatusByChannelId(channelId: Long): List<ReactionStatusItem>
}
