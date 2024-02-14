package com.banglalink.toffee.usecase

import com.banglalink.toffee.notification.ADVERTISING_ID_TOPIC
import com.banglalink.toffee.notification.PubSubMessageUtil
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class SendAdvertisingIdLogEvent @Inject constructor(
    private val json: Json
) {
    
    fun execute(adIdLogData: AdvertisingIdLogData) {
        PubSubMessageUtil.send(adIdLogData, ADVERTISING_ID_TOPIC)
    }
}

@Serializable
data class AdvertisingIdLogData(
    @SerialName("advertising_id")
    val advertisingId: String? = null
): HeaderEnrichmentLogData(){
    override var phoneNumber: String
        get() = super.phoneNumber
        set(value) {super.phoneNumber = value}
    
    override var isBlNumber: String
        get() = super.isBlNumber
        set(value) {super.isBlNumber = value}
}