package com.banglalink.toffee.data.database.entities

import androidx.room.Entity
import androidx.room.Ignore
import com.banglalink.toffee.di.NetworkModuleLib
import com.banglalink.toffee.model.ChannelInfo
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class TVChannelItem(
    @SerialName("channelId")
    val channelId: Long,
    @SerialName("type")
    val type: String,
    @SerialName("priority")
    val priority: Int,
    @SerialName("categoryName")
    val categoryName: String,
    @SerialName("payload")
    val payload: String,
    @SerialName("viewCount")
    val viewCount: Long,
    @SerialName("isStingray")
    val isStingray: Boolean = false,
    @SerialName("isFmRadio")
    val isFmRadio: Boolean = false
) : BaseEntity() {
    @Ignore
    @SerialName("channelInfo")
    val channelInfo: ChannelInfo? = try {
        NetworkModuleLib.providesJsonWithConfig().decodeFromString<ChannelInfo>(payload)
    } catch (ex: Exception) {
        null
    }
}