package com.banglalink.toffee.data.database.entities

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import com.banglalink.toffee.model.ChannelInfo
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

@Entity(indices = [Index(value = ["customerId", "channelId"], unique = true)])
data class ContinueWatchingItem(
    @SerializedName("customerId")
    val customerId: Int,
    @SerializedName("channelId")
    val channelId: Long,
    @SerializedName("type")
    val type: String,
    @SerializedName("categoryId")
    val categoryId: Int,
    @SerializedName("payload")
    val payload: String,
    @SerializedName("progress")
    val progress: Long
) : BaseEntity() {
    @Ignore
    @SerializedName("channelInfo")
    val channelInfo: ChannelInfo? = try {
        Gson().fromJson(payload, ChannelInfo::class.java)
    } catch (ex: Exception) {
        null
    }
}