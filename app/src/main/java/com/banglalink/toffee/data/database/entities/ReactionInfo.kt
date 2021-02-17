package com.banglalink.toffee.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.banglalink.toffee.extension.toFormattedDate
import com.google.gson.annotations.SerializedName

@Entity
data class ReactionInfo (
    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    val id: Long? = null,
    @SerializedName("customer_id")
    val customerId: Int,
    @SerializedName("content_id")
    val contentId: Long,
    @SerializedName("reaction_type")
    val reactionType: Int,
    @SerializedName("reaction_time")
    val reactionTime: Long = System.currentTimeMillis()
){
    fun getReactionDate(): String {
        return reactionTime.toFormattedDate()
    }
}