package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.notification.PubSubMessageUtil
import com.banglalink.toffee.notification.USER_INTEREST_TOPIC
import com.banglalink.toffee.util.currentDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class SendUserInterestEvent @Inject constructor(
    private val json: Json,
    private val preference: SessionPreference,
) {
    
    suspend fun execute(interestList: Map<String, Int>, sendToPubSub: Boolean = true) {
        val interestData = InterestData(preference.customerId, interestList)
        PubSubMessageUtil.sendMessage(json.encodeToString(interestData), USER_INTEREST_TOPIC)
    }
}

@Serializable
data class InterestData(
    @SerialName("user_id")
    val customerId: Int = 0,
    @SerialName("interest_list")
    val interestList: Map<String, Int>? = null,
    @SerialName("device_type")
    val deviceType: Int = 1,
    @SerialName("device_id")
    val deviceId: String = CommonPreference.getInstance().deviceId,
    @SerialName("date_time")
    val interestDateTime: String = currentDateTime,
    @SerialName("reportingTime")
    val reportingTime: String = currentDateTime
)
