package com.banglalink.toffee.data.database.entities

import androidx.room.Entity
import androidx.room.Ignore
import com.banglalink.toffee.model.ChannelInfo
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

@Entity
data class TVChannelItem(
    @SerializedName("channelId")
    val channelId: Long,
    @SerializedName("type")
    val type: String,
    @SerializedName("priority")
    val priority: Int,
    @SerializedName("categoryName")
    val categoryName: String,
    @SerializedName("payload")
    val payload: String,
    @SerializedName("viewCount")
    val viewCount: Long,
    @SerializedName("isStingray")
    val isStingray: Boolean = false,
    @SerializedName("isFmRadio")
    val isFmRadio: Boolean = false
) : BaseEntity() {
    @Ignore
    @SerializedName("channelInfo")
    val channelInfo: ChannelInfo? = try {
        Gson().fromJson(payload, ChannelInfo::class.java)
    } catch (ex: Exception) {
        null
    }
}