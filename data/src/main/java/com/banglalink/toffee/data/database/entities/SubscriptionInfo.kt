package com.banglalink.toffee.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.banglalink.toffee.extension.toFormattedDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class SubscriptionInfo(
    @PrimaryKey(autoGenerate = true)
    @SerialName("id")
    val id: Long? = null,
    @SerialName("channel_id")
    @ColumnInfo(defaultValue = "0")
    val channelId: Int = 0,
    @SerialName("customer_id")
    @ColumnInfo(defaultValue = "0")
    val customerId: Int = 0,
    @SerialName("date_time")
    val dateTime: Long = System.currentTimeMillis()
) {
    fun getDate(): String {
        return dateTime.toFormattedDate()
    }
}