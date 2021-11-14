package com.banglalink.toffee.data.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(indices = [Index(value = ["channelId"], unique = true)])
data class FavoriteItem(
    @SerializedName("id")
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    @SerializedName("channelId")
    val channelId: Long,
    @SerializedName("isFavorite")
    val isFavorite: Int
)