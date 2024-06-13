package com.banglalink.toffee.data.database.entities

import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
abstract class BaseEntity {
    @SerialName("id")
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    @SerialName("createTime")
    var createTime: Long = System.currentTimeMillis()
    @SerialName("updateTime")
    var updateTime: Long = System.currentTimeMillis()
}