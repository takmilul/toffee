package com.banglalink.toffee.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(
        tableName = "subscription_info"
)
data class SubscriptionInfo(

     @PrimaryKey(autoGenerate = true)
     @SerializedName("id")
     val id: Long? = null,
     @SerializedName("channel_id")
     val channelId: Int,
     @SerializedName("customer_id")
     val customerId: Int,
     @SerializedName("status")
     val status: Int
)