package com.banglalink.toffee.data.database.entities

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import com.banglalink.toffee.di.NetworkModuleLib
import com.banglalink.toffee.model.ChannelInfo
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Entity(indices = [Index(value = ["customerId", "channelId"], unique = true)])
data class ContinueWatchingItem(
    @SerialName("customerId")
    val customerId: Int,
    @SerialName("channelId")
    val channelId: Long,
    @SerialName("type")
    val type: String,
    @SerialName("categoryId")
    val categoryId: Int,
    @SerialName("payload")
    val payload: String,
    @SerialName("progress")
    val progress: Long
) : BaseEntity() {
    @Ignore
    @SerialName("channelInfo")
    val channelInfo: ChannelInfo? = try {
        NetworkModuleLib.providesJsonWithConfig().decodeFromString<ChannelInfo>(payload)
    } catch (ex: Exception) {
        null
    }
}