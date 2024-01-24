package com.banglalink.toffee.data.database.entities

import androidx.room.Entity
import androidx.room.Ignore
import com.banglalink.toffee.di.NetworkModuleLib
import com.banglalink.toffee.model.ChannelInfo
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class CdnChannelItem(
    @SerialName("channelId")
    val channelId: Long = 0,
    @SerialName("urlType")
    val urlType: Int = 0,
    @SerialName("expiryDate")
    var expiryDate: String? = null,
    @SerialName("payload")
    var payload: String? = null,
) : BaseEntity() {
    @Ignore
    @SerialName("channelInfo")
    val channelInfo: ChannelInfo? = try {
        payload?.let { NetworkModuleLib.providesJsonWithConfig().decodeFromString<ChannelInfo>(it) }
    } catch (ex: Exception) {
        null
    }
}