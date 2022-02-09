package com.banglalink.toffee.data.database.entities

import androidx.room.Entity
import androidx.room.Ignore
import com.banglalink.toffee.model.ChannelInfo
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

@Entity
data class HistoryItem(
    @SerializedName("channelId")
    val channelId: Long,
    @SerializedName("type")
    val type: String = "",
    @SerializedName("category")
    val category: String = "",
    @SerializedName("payload")
    val payload: String = ""
) : BaseEntity() {
    
    @Ignore
    @SerializedName("channelInfo")
    val channelInfo: ChannelInfo? = try {
        Gson().fromJson(payload, ChannelInfo::class.java)
    } catch (ex: Exception) {
        null
    }
}