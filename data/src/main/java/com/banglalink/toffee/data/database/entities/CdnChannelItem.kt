package com.banglalink.toffee.data.database.entities

import androidx.room.Entity
import androidx.room.Ignore
import com.banglalink.toffee.di.NetworkModuleLib
import com.banglalink.toffee.model.ChannelInfo
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Entity
@Serializable
data class CdnChannelItem(
    @SerialName("channelId")
    val channelId: Long,
    @SerialName("urlType")
    val urlType: Int,
    @SerialName("expiryDate")
    var expiryDate: String? = null,
    @SerialName("payload")
    var payload: String,
) : BaseEntity() {
    @Ignore
    @SerialName("channelInfo")
    val channelInfo: ChannelInfo? = try {
        NetworkModuleLib.providesJsonWithConfig().decodeFromString<ChannelInfo>(payload)
    } catch (ex: Exception) {
        null
    }
}