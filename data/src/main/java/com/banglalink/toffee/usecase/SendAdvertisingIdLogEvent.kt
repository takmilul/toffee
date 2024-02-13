package com.banglalink.toffee.usecase

import com.banglalink.toffee.notification.ADVERTISING_ID_TOPIC
import com.banglalink.toffee.notification.PubSubMessageUtil
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class SendAdvertisingIdLogEvent @Inject constructor() {
    
    fun execute(adIdLogData: AdvertisingIdLogData) {
        PubSubMessageUtil.send(adIdLogData, ADVERTISING_ID_TOPIC)
    }
}

data class AdvertisingIdLogData(
    @SerializedName("advertising_id")
    val advertisingId: String? = null
): HeaderEnrichmentLogData(){
    override var phoneNumber: String
        get() = super.phoneNumber
        set(value) {super.phoneNumber = value}
    
    override var isBlNumber: String
        get() = super.isBlNumber
        set(value) {super.isBlNumber = value}
}