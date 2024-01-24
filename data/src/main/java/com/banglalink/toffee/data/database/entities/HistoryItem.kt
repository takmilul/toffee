package com.banglalink.toffee.data.database.entities

import androidx.room.Entity
import androidx.room.Ignore
import com.banglalink.toffee.di.NetworkModuleLib
import com.banglalink.toffee.model.ChannelInfo
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class HistoryItem(
    @SerialName("channelId")
    val channelId: Long = 0,
    @SerialName("type")
    val type: String = "",
    @SerialName("category")
    val category: String = "",
    @SerialName("payload")
    val payload: String = ""
) : BaseEntity() {
    
    @Ignore
    @SerialName("channelInfo")
    val channelInfo: ChannelInfo? = try {
        NetworkModuleLib.providesJsonWithConfig().decodeFromString<ChannelInfo>(payload)
    } catch (ex: Exception) {
        null
    }
}