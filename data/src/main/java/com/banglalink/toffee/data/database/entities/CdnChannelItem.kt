package com.banglalink.toffee.data.database.entities

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import com.banglalink.toffee.model.ChannelInfo
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

@Entity(indices = [Index(value = ["channelId"], unique = true)])
data class CdnChannelItem(
    @SerializedName("channelId")
    val channelId: Long,
    @SerializedName("urlType")
    val urlType: Int,
    @SerializedName("expiryDate")
    var expiryDate: String? = null,
    @SerializedName("payload")
    var payload: String,
) : BaseEntity() {
    @Ignore
    @SerializedName("channelInfo")
    val channelInfo: ChannelInfo? = try {
        Gson().fromJson(payload, ChannelInfo::class.java)
    } catch (ex: Exception) {
        null
    }
}