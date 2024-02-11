package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.notification.PubSubMessageUtil
import com.banglalink.toffee.notification.USER_INTEREST_TOPIC
import com.banglalink.toffee.util.currentDateTime
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class SendUserInterestEvent @Inject constructor(
    private val preference: SessionPreference,
) {
    
    suspend fun execute(interestList: Map<String, Int>, sendToPubSub: Boolean = true) {
        val interestData = InterestData(preference.customerId, interestList)
        PubSubMessageUtil.send(interestData, USER_INTEREST_TOPIC)
    }
}

data class InterestData(
    @SerializedName("user_id")
    val customerId: Int,
    @SerializedName("interest_list")
    val interestList: Map<String, Int>,
    @SerializedName("device_type")
    val deviceType: Int = 1,
    @SerializedName("device_id")
    val deviceId: String = CommonPreference.getInstance().deviceId,
    @SerializedName("date_time")
    val interestDateTime: String = currentDateTime,
    @SerializedName("reportingTime")
    val reportingTime: String = currentDateTime
)
