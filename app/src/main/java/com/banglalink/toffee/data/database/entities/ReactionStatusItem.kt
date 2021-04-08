package com.banglalink.toffee.data.database.entities

import androidx.room.Entity
import androidx.room.Index

@Entity(
    indices = [Index(value = ["contentId", "reactionType"], unique = true)]
)
data class ReactionStatusItem(
    val contentId: Int = 0,
    val reactionType: Int = 0,
    val reactionCount: Long = 0L,
): BaseEntity()