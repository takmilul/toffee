package com.banglalink.toffee.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class ShareCount(
    @PrimaryKey
    @SerialName("content_id")
    val contentId: Int = 0,
    @SerialName("count")
    var count: Long = 0L,
)