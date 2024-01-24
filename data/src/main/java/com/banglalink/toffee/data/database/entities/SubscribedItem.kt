package com.banglalink.toffee.data.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Entity(indices = [Index(value = ["channelId"], unique = true)])
data class SubscribedItem(
    @SerialName("id")
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    @SerialName("channelId")
    val channelId: Long = 0,
    @SerialName("isFavorite")
    val isFavorite: Int = 0
)