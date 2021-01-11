package com.banglalink.toffee.data.database.entities

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import com.banglalink.toffee.model.ChannelInfo
import com.google.gson.Gson

@Entity (indices = [Index(value = ["customerId", "channelId","activityType","activitySubType"], unique = true)])
data class UserActivities(
    val customerId: Int = 0,
    val channelId: Long,
    val category: String,
    val type: String,
    val payload: String,
    val activityType: Int,
    val activitySubType: Int,
): BaseEntity() {
    @Ignore
    val channelInfo: ChannelInfo? = try {
        Gson().fromJson(payload, ChannelInfo::class.java)
    } catch (ex: Exception) {
        null
    }
}