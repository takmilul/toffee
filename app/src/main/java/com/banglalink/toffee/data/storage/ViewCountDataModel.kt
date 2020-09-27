package com.banglalink.toffee.data.storage

import androidx.room.*

@Entity(
    tableName = "channel_view_count",
    indices = [Index(value = arrayOf("channel_id"), unique = true)]
)
class ViewCountDataModel{

    @PrimaryKey
    @ColumnInfo(name = "channel_id")
    var channelId: Int = 0

    @ColumnInfo(name = "view_count")
    var viewCount: Long = 0

}