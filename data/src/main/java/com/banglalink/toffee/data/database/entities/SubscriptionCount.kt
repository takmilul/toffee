package com.banglalink.toffee.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class SubscriptionCount(
    @PrimaryKey
    @SerialName("channel_id")
    val channelId: Int = 0,
    @SerialName("status")
    var status: Long = 0L
)