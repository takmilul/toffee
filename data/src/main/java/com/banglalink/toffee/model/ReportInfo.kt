package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReportInfo(
    @SerialName("content_id")
    val contentId: Long,
    @SerialName("offense_type_id")
    val offenseTypeId: Int,
    @SerialName("time_stamp")
    val timeStamp: String,
    @SerialName("additional_detail")
    val additionalDetail: String? = null,
    @SerialName("offense_id")
    val offenseId: Int = 0,
)