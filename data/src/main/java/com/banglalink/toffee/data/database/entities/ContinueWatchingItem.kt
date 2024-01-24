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
    val customerId: Int = 0,
    @SerialName("channelId")
    val channelId: Long = 0,
    @SerialName("type")
    val type: String? = null,
    @SerialName("categoryId")
    val categoryId: Int = 0,
    @SerialName("payload")
    val payload: String? = null,
    @SerialName("progress")
    val progress: Long = 0
) : BaseEntity() {
    @Ignore
    @SerialName("channelInfo")
    val channelInfo: ChannelInfo? = try {
        payload?.let { NetworkModuleLib.providesJsonWithConfig().decodeFromString<ChannelInfo>(it) }
    } catch (ex: Exception) {
        null
    }
}