package com.banglalink.toffee.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.banglalink.toffee.extension.toFormattedDate
import com.google.gson.annotations.SerializedName

@Entity
data class SubscriptionInfo (

    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    val id: Long? = null,
    @SerializedName("channel_id")
    val channelId: Int,
    @SerializedName("subscriber_id")
    val subscriberId: Int,
    @SerializedName("status")
    val status: Int,
    @SerializedName("date_time")
    val dateTime: Long = System.currentTimeMillis()
){
    fun getDate(): String {
        return dateTime.toFormattedDate()
    }
}