package com.banglalink.toffee.data.storage

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.util.*

abstract class BaseModel {

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    @ColumnInfo(name = "creation_date")
    @TypeConverters(DateConverter::class)
    var creationDate: Date = Date(System.currentTimeMillis())

    @ColumnInfo(name = "modification_date")
    @TypeConverters(DateConverter::class)
    var modificationDate: Date = Date(System.currentTimeMillis())

}