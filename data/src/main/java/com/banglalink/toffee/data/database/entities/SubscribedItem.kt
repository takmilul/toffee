package com.banglalink.toffee.data.database.entities

import androidx.room.ColumnInfo
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
    @ColumnInfo(defaultValue = "0")
    val channelId: Long = 0,
    @SerialName("isFavorite")
    @ColumnInfo(defaultValue = "0")
    val isFavorite: Int = 0
)