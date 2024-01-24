package com.banglalink.toffee.usecase

import android.os.Build
import com.banglalink.toffee.Constants
import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.ReportInfo
import com.banglalink.toffee.notification.CONTENT_REPORT_TOPIC
import com.banglalink.toffee.notification.PubSubMessageUtil
import com.banglalink.toffee.util.currentDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class SendContentReportEvent @Inject constructor(
    private val json: Json,
    private val preference: SessionPreference,
) {
    
    fun execute(reportInfo: ReportInfo) {
        val reportData = ReportInAppropriateVideoData(preference.customerId, reportInfo.contentId, reportInfo.offenseTypeId, reportInfo.offenseId, 
            reportInfo.timeStamp, reportInfo.additionalDetail)
        PubSubMessageUtil.sendMessage(json.encodeToString(reportData), CONTENT_REPORT_TOPIC)
    }
}

@Serializable
data class ReportInAppropriateVideoData(
    @SerialName("customer_id")
    val customerId: Int = 0,
    @SerialName("content_id")
    val contentId: Long = 0,
    @SerialName("offense_type_id")
    val offenseTypeId: Int = 0,
    @SerialName("offense_id")
    val offenseId: Int = 0,
    @SerialName("time_stamp")
    val timeStamp: String? = null,
    @SerialName("additional_detail")
    val additionalDetail: String? = null,
    @SerialName("report_time")
    val reportTime: String = currentDateTime,
    @SerialName("device_type")
    val deviceType :Int = Constants.DEVICE_TYPE,
    @SerialName("device_id")
    val deviceId: String = CommonPreference.getInstance().deviceId,
    @SerialName("app_version")
    val appVersion : String = CommonPreference.getInstance().appVersionName,
    @SerialName("os_version")
    val osVersion :String = "android "+ Build.VERSION.RELEASE,
    @SerialName("reportingTime")
    val reportingTime: String = currentDateTime
)
