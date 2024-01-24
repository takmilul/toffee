package com.banglalink.toffee.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.banglalink.toffee.extension.toFormattedDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class ReactionInfo(
    @PrimaryKey(autoGenerate = true)
    @SerialName("id")
    val id: Long? = null,
    @SerialName("customer_id")
    val customerId: Int = 0,
    @SerialName("content_id")
    val contentId: Long = 0,
    @SerialName("reaction_type")
    val reactionType: Int = 0,
    @SerialName("reaction_time")
    val reactionTime: Long = System.currentTimeMillis()
) {
    fun getReactionDate(): String {
        return reactionTime.toFormattedDate()
    }
}