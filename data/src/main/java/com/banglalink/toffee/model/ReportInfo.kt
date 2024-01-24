package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReportInfo(
    @SerialName("content_id")
    val contentId: Long = 0,
    @SerialName("offense_type_id")
    val offenseTypeId: Int = 0,
    @SerialName("time_stamp")
    val timeStamp: String? = null,
    @SerialName("additional_detail")
    val additionalDetail: String? = null,
    @SerialName("offense_id")
    val offenseId: Int = 0,
)