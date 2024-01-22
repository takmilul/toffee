package com.banglalink.toffee.data.database.entities

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
    val customerId: Int,
    @SerialName("contentId")
    val contentId: Long,
    @SerialName("progress")
    val progress: Long,
    @SerialName("watchTime")
    val watchTime: Long = System.currentTimeMillis(),
)