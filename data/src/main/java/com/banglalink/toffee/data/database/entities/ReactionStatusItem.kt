package com.banglalink.toffee.data.database.entities

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
    val contentId: Int = 0,
    @SerialName("reactionType")
    val reactionType: Int = 0,
    @SerialName("reactionCount")
    var reactionCount: Long = 0L,
) : BaseEntity()