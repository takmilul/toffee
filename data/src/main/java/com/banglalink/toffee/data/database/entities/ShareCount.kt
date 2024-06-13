package com.banglalink.toffee.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class ShareCount(
    @PrimaryKey
    @SerialName("content_id")
    @ColumnInfo(defaultValue = "0")
    val contentId: Int = 0,
    @SerialName("count")
    @ColumnInfo(defaultValue = "0")
    var count: Long = 0L,
)