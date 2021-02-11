package com.banglalink.toffee.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.banglalink.toffee.extension.toFormattedDate

@Entity
data class ReactionInfo (
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    val customerId: Int,
    val contentId: Long,
    val reactionType: Int,
    val reactionTime: Long = System.currentTimeMillis()
){
    fun getReactionDate(): String {
        return reactionTime.toFormattedDate()
    }
}