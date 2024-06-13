package com.banglalink.toffee.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Entity(
    indices = [Index(value = ["contentId", "reactionType"], unique = true)]
)
@Serializable
data class ReactionStatusItem(
    @SerialName("contentId")
    @ColumnInfo(defaultValue = "0")
    val contentId: Int = 0,
    @SerialName("reactionType")
    @ColumnInfo(defaultValue = "0")
    val reactionType: Int = 0,
    @SerialName("reactionCount")
    @ColumnInfo(defaultValue = "0")
    var reactionCount: Long = 0L,
) : BaseEntity()