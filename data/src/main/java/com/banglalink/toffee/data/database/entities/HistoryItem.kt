package com.banglalink.toffee.data.database.entities

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.banglalink.toffee.model.ChannelInfo
import com.google.gson.Gson

@Entity
data class HistoryItem(
    val channelId: Long,
    val type: String = "",
    val category: String = "",
    val payload: String = ""
): BaseEntity() {
    fun isLive() = type.equals("LIVE", ignoreCase = true)

    @Ignore
    val channelInfo: ChannelInfo? = try {
            Gson().fromJson(payload, ChannelInfo::class.java)
        } catch (ex: Exception) {
            null
        }
}