package com.banglalink.toffee.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ReactionInfo (
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    val contentId: String,
    val reaction: Int,
    val reactionTime: Long = System.currentTimeMillis()
)