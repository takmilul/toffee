package com.banglalink.toffee.data.database.entities

import androidx.room.Entity
import androidx.room.Index
import com.google.gson.annotations.SerializedName

@Entity(
    indices = [Index(value = ["contentId", "reactionType"], unique = true)]
)
data class ReactionStatusItem(
    @SerializedName("contentId")
    val contentId: Int = 0,
    @SerializedName("reactionType")
    val reactionType: Int = 0,
    @SerializedName("reactionCount")
    var reactionCount: Long = 0L,
) : BaseEntity()