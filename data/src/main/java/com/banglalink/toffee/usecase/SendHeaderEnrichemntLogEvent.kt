package com.banglalink.toffee.usecase

import android.os.Build
import com.banglalink.toffee.data.network.request.PubSubBaseRequest
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.notification.HE_REPORT_TOPIC
import com.banglalink.toffee.notification.PubSubMessageUtil
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class SendHeaderEnrichmentLogEvent @Inject constructor(
    private val json: Json
) {
    
    fun execute(heLogData: HeaderEnrichmentLogData) {
        PubSubMessageUtil.send(heLogData, HE_REPORT_TOPIC)
    }
}

@Serializable
open class HeaderEnrichmentLogData(
    @SerialName("device")
    val device: String = Build.MANUFACTURER,
    @SerialName("deviceModel")
    val deviceModel: String = Build.MODEL,
    @SerialName("lat")
    val lat: String = SessionPreference.getInstance().latitude,
    @SerialName("lon")
    val lon: String = SessionPreference.getInstance().longitude,
    @SerialName("user_ip")
    val userIp: String = SessionPreference.getInstance().userIp,
    @SerialName("geo_city")
    val geoCity: String = SessionPreference.getInstance().geoCity,
    @SerialName("geo_location")
    val geoLocation: String = SessionPreference.getInstance().geoLocation,
): PubSubBaseRequest(){
    override var phoneNumber: String
        get() = super.phoneNumber
        set(value) {super.phoneNumber = value}
    
    override var isBlNumber: String
        get() = super.isBlNumber
        set(value) {super.isBlNumber = value}
}