package com.banglalink.toffee.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Entity(indices = [Index(value = ["customerId", "contentId"], unique = true)])
data class ContentViewProgress(
    @SerialName("id")
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    @SerialName("customerId")
    @ColumnInfo(defaultValue = "0")
    val customerId: Int = 0,
    @SerialName("contentId")
    @ColumnInfo(defaultValue = "0")
    val contentId: Long = 0,
    @SerialName("progress")
    @ColumnInfo(defaultValue = "0")
    val progress: Long = 0,
    @SerialName("watchTime")
    val watchTime: Long = System.currentTimeMillis(),
)