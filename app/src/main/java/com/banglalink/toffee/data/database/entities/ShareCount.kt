package com.banglalink.toffee.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class ShareCount(
    @PrimaryKey
    @SerializedName("content_id")
    val contentId: Int,

    @SerializedName("count")
    var count: Long = 0L,
)