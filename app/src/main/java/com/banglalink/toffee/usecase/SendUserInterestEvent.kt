package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.extension.toFormattedDate
import com.banglalink.toffee.notification.PubSubMessageUtil
import com.banglalink.toffee.notification.USER_INTEREST_TOPIC
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class SendUserInterestEvent @Inject constructor(
    private val preference: SessionPreference,
) {
    
    private val gson = Gson()
    
    suspend fun execute(interestList: ArrayList<Int>, sendToPubSub: Boolean = true) {
        val shareCount = InterestData(preference.customerId, interestList)
        PubSubMessageUtil.sendMessage(gson.toJson(shareCount), USER_INTEREST_TOPIC)
    }
}

data class InterestData(
    @SerializedName("user_id")
    val customerId: Int,
    @SerializedName("interest_list")
    val interestList: ArrayList<Int>,
    @SerializedName("device_type")
    val deviceType: Int = 1,
    @SerializedName("date_time")
    val interestDateTime: String = System.currentTimeMillis().toFormattedDate(),
)
