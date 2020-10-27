package com.banglalink.toffee.data.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(value = ["channelId"], unique = true)])
data class SubscribedItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,

    val channelId: Long,
    val isFavorite: Int
)