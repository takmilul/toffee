package com.banglalink.toffee.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class SubscriptionCount (
    @PrimaryKey
    @SerializedName("channel_id")
    val channelId: Int,
    @SerializedName("status")
    val status: Long = 0L
)