package com.banglalink.toffee.data.database.entities

import androidx.room.ColumnInfo
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
    @ColumnInfo(defaultValue = "0")
    val channelId: Long = 0,
    @SerialName("type")
    val type: String? = null,
    @SerialName("priority")
    @ColumnInfo(defaultValue = "0")
    val priority: Int = 0,
    @SerialName("categoryName")
    val categoryName: String? = null,
    @SerialName("payload")
    val payload: String? = null,
    @SerialName("viewCount")
    @ColumnInfo(defaultValue = "0")
    val viewCount: Long = 0,
    @SerialName("isStingray")
    val isStingray: Boolean = false,
    @SerialName("isFmRadio")
    val isFmRadio: Boolean = false
) : BaseEntity() {
    @Ignore
    @SerialName("channelInfo")
    val channelInfo: ChannelInfo? = try {
        payload?.let { NetworkModuleLib.providesJsonWithConfig().decodeFromString<ChannelInfo>(it) }
    } catch (ex: Exception) {
        null
    }
}