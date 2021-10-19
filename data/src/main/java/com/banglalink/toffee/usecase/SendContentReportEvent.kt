package com.banglalink.toffee.usecase

import android.os.Build
import com.banglalink.toffee.Constants
import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.extension.toFormattedDate
import com.banglalink.toffee.model.ReportInfo
import com.banglalink.toffee.notification.CONTENT_REPORT_TOPIC
import com.banglalink.toffee.notification.PubSubMessageUtil
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class SendContentReportEvent @Inject constructor(
    private val preference: SessionPreference,
) {

    private val gson = Gson()

    fun execute(reportInfo: ReportInfo) {
        val reportData = ReportInAppropriateVideoData(preference.customerId, reportInfo.contentId, reportInfo.offenseTypeId, reportInfo.offenseId, 
            reportInfo.timeStamp, reportInfo.additionalDetail)
        PubSubMessageUtil.sendMessage(gson.toJson(reportData), CONTENT_REPORT_TOPIC)
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
    val reportTime: String = System.currentTimeMillis().toFormattedDate(),
    @SerializedName("device_type")
    val deviceType :Int = 1,
    @SerializedName("device_id")
    val deviceId: String = CommonPreference.getInstance().deviceId,
    @SerializedName("app_version")
    val appVersion : String = Constants.VERSION_NAME,
    @SerializedName("os_version")
    val osVersion :String = "android "+ Build.VERSION.RELEASE
)
