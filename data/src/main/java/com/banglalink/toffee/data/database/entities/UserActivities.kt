package com.banglalink.toffee.data.database.entities

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import com.banglalink.toffee.di.NetworkModuleLib
import com.banglalink.toffee.model.ChannelInfo
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Entity(indices = [Index(value = ["customerId", "channelId", "activityType", "activitySubType"], unique = true)])
data class UserActivities(
    @SerialName("customerId")
    val customerId: Int = 0,
    @SerialName("channelId")
    val channelId: Long,
    @SerialName("category")
    val category: String,
    @SerialName("type")
    val type: String,
    @SerialName("payload")
    val payload: String,
    @SerialName("activityType")
    val activityType: Int,
    @SerialName("activitySubType")
    val activitySubType: Int,
) : BaseEntity() {
    @Ignore
    @SerialName("channelInfo")
    val channelInfo: ChannelInfo? = try {
        NetworkModuleLib.providesJsonWithConfig().decodeFromString<ChannelInfo>(payload)
    } catch (ex: Exception) {
        null
    }
}