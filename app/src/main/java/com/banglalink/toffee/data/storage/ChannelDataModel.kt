package com.banglalink.toffee.data.storage

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index

@Entity(
    tableName = "channels",
    indices = [Index(value = arrayOf("id"), unique = true)]
)
class ChannelDataModel(): BaseModel() {

    @ColumnInfo(name = "channel_id")
    var channelId: Int = 0

    @ColumnInfo(name = "payload")
    var payLoad: String = String()

    @ColumnInfo(name = "type")
    var type: String = String()

    @ColumnInfo(name = "category")
    var category: String = String()

}