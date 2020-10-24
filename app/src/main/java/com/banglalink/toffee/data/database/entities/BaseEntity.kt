package com.banglalink.toffee.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey

abstract class BaseEntity {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    var createTime: Long = System.currentTimeMillis()
    var updateTime: Long = System.currentTimeMillis()
}