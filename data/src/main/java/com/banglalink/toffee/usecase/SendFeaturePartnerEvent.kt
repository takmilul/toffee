package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.PubSubBaseRequest
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.notification.FEATURE_PARTNER_LOG
import com.banglalink.toffee.notification.PubSubMessageUtil
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class SendFeaturePartnerEvent @Inject constructor(
    private val json: Json,
    private val preference: SessionPreference,
) {
    
    fun execute(partnerName: String,partnerId: Int) {
        val reportData = ReportFeaturePartnerData(
            lastLoginDateTime = preference.lastLoginDateTime,
            partnerName = partnerName,
            partnerId =partnerId,
            isLoggedIn = if(preference.isVerifiedUser) 1 else 0
        )
        PubSubMessageUtil.sendMessage(json.encodeToString(reportData), FEATURE_PARTNER_LOG)
    }
}

@Serializable
data class ReportFeaturePartnerData(
    @SerialName("last_login_date_time")
    val lastLoginDateTime: String? = null,
    @SerialName("partner_name")
    val partnerName: String? = null,
    @SerialName("partnerId")
    val partnerId: Int = 0,
    @SerialName("isLoggedIn")
    val isLoggedIn: Int = 0,
    ) : PubSubBaseRequest()
