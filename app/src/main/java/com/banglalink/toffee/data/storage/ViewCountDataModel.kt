package com.banglalink.toffee.data.storage

import androidx.room.*

@Entity(
    tableName = "channel_view_count"
)
class ViewCountDataModel{

    @PrimaryKey
    @ColumnInfo(name = "channel_id")
    var channelId: Long = 0

    @ColumnInfo(name = "view_count")
    var viewCount: Long = 0

}