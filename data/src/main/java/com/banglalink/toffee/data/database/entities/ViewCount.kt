package com.banglalink.toffee.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Entity(
    tableName = "channel_view_count"
)
@Serializable
class ViewCount {
    @SerialName("channelId")
    @PrimaryKey
    @ColumnInfo(name = "channel_id")
    var channelId: Long = 0
    @SerialName("viewCount")
    @ColumnInfo(name = "view_count")
    var viewCount: Long = 0
}