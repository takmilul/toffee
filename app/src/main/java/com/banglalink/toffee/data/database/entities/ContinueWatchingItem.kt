package com.banglalink.toffee.data.database.entities

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import com.banglalink.toffee.model.ChannelInfo
import com.google.gson.Gson

@Entity(indices = [Index(value = ["customerId", "channelId"], unique = true)])
data class ContinueWatchingItem(
    val customerId: Int,
    val channelId: Long,
    val type: String,
    val categoryId: Int,
    val payload: String,
    val progress: Long
): BaseEntity() {
    @Ignore
    val channelInfo: ChannelInfo? = try {
        Gson().fromJson(payload, ChannelInfo::class.java)
    } catch (ex: Exception) {
        null
    }
}