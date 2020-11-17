package com.banglalink.toffee.data.database.entities

import androidx.room.Entity
import androidx.room.Ignore
import com.banglalink.toffee.model.ChannelInfo
import com.google.gson.Gson

@Entity
data class TVChannelItem(
    val channelId: Long,
    val type: String,
    val priority: Int,
    val categoryName: String,
    val payload: String
): BaseEntity() {
    @Ignore
    val channelInfo: ChannelInfo? = try {
        Gson().fromJson(payload, ChannelInfo::class.java)
    } catch (ex: Exception) {
        null
    }
}