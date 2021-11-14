package com.banglalink.toffee.data.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(indices = [Index(value = ["customerId", "contentId"], unique = true)])
data class ContentViewProgress(
    @SerializedName("id")
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    @SerializedName("customerId")
    val customerId: Int,
    @SerializedName("contentId")
    val contentId: Long,
    @SerializedName("progress")
    val progress: Long,
    @SerializedName("watchTime")
    val watchTime: Long = System.currentTimeMillis(),
)