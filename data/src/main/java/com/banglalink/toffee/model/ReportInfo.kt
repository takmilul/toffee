package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class ReportInfo(
    @SerializedName("content_id")
    val contentId: Long,
    @SerializedName("offense_type_id")
    val offenseTypeId: Int,
    @SerializedName("time_stamp")
    val timeStamp: String,
    @SerializedName("additional_detail")
    val additionalDetail: String? = null,
    @SerializedName("offense_id")
    val offenseId: Int = 0,
)