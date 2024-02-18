package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.PubSubBaseRequest
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.notification.FEATURE_PARTNER_LOG
import com.banglalink.toffee.notification.PubSubMessageUtil
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class SendFeaturePartnerEvent @Inject constructor(
    private val preference: SessionPreference,
) {
    
    fun execute(partnerName: String,partnerId: Int) {
        val reportData = ReportFeaturePartnerData(
            lastLoginDateTime = preference.lastLoginDateTime,
            partnerName = partnerName,
            partnerId =partnerId,
            isLoggedIn = if(preference.isVerifiedUser) 1 else 0
        )
        PubSubMessageUtil.send(reportData, FEATURE_PARTNER_LOG)
    }
}

data class ReportFeaturePartnerData(
    @SerializedName("last_login_date_time")
    val lastLoginDateTime: String,
    @SerializedName("partner_name")
    val partnerName: String,
    @SerializedName("partnerId")
    val partnerId: Int,
    @SerializedName("isLoggedIn")
    val isLoggedIn: Int,
    ) : PubSubBaseRequest()
