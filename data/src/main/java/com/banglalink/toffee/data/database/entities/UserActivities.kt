package com.banglalink.toffee.data.database.entities

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import com.banglalink.toffee.model.ChannelInfo
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

@Entity(indices = [Index(value = ["customerId", "channelId", "activityType", "activitySubType"], unique = true)])
data class UserActivities(
    @SerializedName("customerId")
    val customerId: Int = 0,
    @SerializedName("channelId")
    val channelId: Long,
    @SerializedName("category")
    val category: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("payload")
    val payload: String,
    @SerializedName("activityType")
    val activityType: Int,
    @SerializedName("activitySubType")
    val activitySubType: Int,
) : BaseEntity() {
    @Ignore
    @SerializedName("channelInfo")
    val channelInfo: ChannelInfo? = try {
        Gson().fromJson(payload, ChannelInfo::class.java)
    } catch (ex: Exception) {
        null
    }
}