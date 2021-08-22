package com.banglalink.toffee.usecase

import android.os.Build
import com.banglalink.toffee.data.network.request.PubSubBaseRequest
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.notification.HE_REPORT_TOPIC
import com.banglalink.toffee.notification.PubSubMessageUtil
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class SendHeaderEnrichmentLogEvent @Inject constructor() {
    private val gson = Gson()
    
    fun execute(heLogData: HeaderEnrichmentLogData) {
        PubSubMessageUtil.sendMessage(gson.toJson(heLogData), HE_REPORT_TOPIC)
    }
}

data class HeaderEnrichmentLogData(
    val device: String = Build.MANUFACTURER,
    val deviceModel: String = Build.MODEL,
    val lat: String = SessionPreference.getInstance().latitude,
    val lon: String = SessionPreference.getInstance().longitude,
    @SerializedName("user_ip")
    val userIp: String = SessionPreference.getInstance().userIp,
    @SerializedName("geo_city")
    val geoCity: String = SessionPreference.getInstance().geoCity,
    @SerializedName("geo_location")
    val geoLocation: String = SessionPreference.getInstance().geoLocation,
): PubSubBaseRequest(){
    override var phoneNumber: String
        get() = super.phoneNumber
        set(value) {super.phoneNumber = value}
    
    override var isBlNumber: String
        get() = super.isBlNumber
        set(value) {super.isBlNumber = value}
}