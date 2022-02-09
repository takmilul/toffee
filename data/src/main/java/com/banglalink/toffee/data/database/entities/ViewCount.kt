package com.banglalink.toffee.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(
    tableName = "channel_view_count"
)
class ViewCount {
    @SerializedName("channelId")
    @PrimaryKey
    @ColumnInfo(name = "channel_id")
    var channelId: Long = 0
    @SerializedName("viewCount")
    @ColumnInfo(name = "view_count")
    var viewCount: Long = 0
}