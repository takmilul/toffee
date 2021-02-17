package com.banglalink.toffee.data.database.entities

import androidx.room.*

@Entity(
    tableName = "reaction_status_item"
)
data class ReactionStatusItem(
    @PrimaryKey
    @ColumnInfo(name = "channel_id")
    val channelId: Long = 0L,

    @ColumnInfo(name = "reaction_type")
    val reactionType: Int = 0,

    @ColumnInfo(name = "reaction_count")
    val reactionCount: Long = 0L,
)