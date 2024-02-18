package com.banglalink.toffee.usecase

import android.os.Build
import com.banglalink.toffee.Constants
import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.ReportInfo
import com.banglalink.toffee.notification.CONTENT_REPORT_TOPIC
import com.banglalink.toffee.notification.PubSubMessageUtil
import com.banglalink.toffee.util.currentDateTime
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class SendContentReportEvent @Inject constructor(
    private val preference: SessionPreference,
) {
    
    fun execute(reportInfo: ReportInfo) {
        val reportData = ReportInAppropriateVideoData(preference.customerId, reportInfo.contentId, reportInfo.offenseTypeId, reportInfo.offenseId, 
            reportInfo.timeStamp, reportInfo.additionalDetail)
        PubSubMessageUtil.send(reportData, CONTENT_REPORT_TOPIC)
    }
}

data class ReportInAppropriateVideoData(
    @SerializedName("customer_id")
    val customerId: Int,
    @SerializedName("content_id")
    val contentId: Long,
    @SerializedName("offense_type_id")
    val offenseTypeId: Int,
    @SerializedName("offense_id")
    val offenseId: Int,
    @SerializedName("time_stamp")
    val timeStamp: String,
    @SerializedName("additional_detail")
    val additionalDetail: String?,
    @SerializedName("report_time")
    val reportTime: String = currentDateTime,
    @SerializedName("device_type")
    val deviceType :Int = Constants.DEVICE_TYPE,
    @SerializedName("device_id")
    val deviceId: String = CommonPreference.getInstance().deviceId,
    @SerializedName("app_version")
    val appVersion : String = CommonPreference.getInstance().appVersionName,
    @SerializedName("os_version")
    val osVersion :String = "android "+ Build.VERSION.RELEASE,
    @SerializedName("reportingTime")
    val reportingTime: String = currentDateTime
)
