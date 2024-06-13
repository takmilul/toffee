package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OffenseBean(
    @SerialName("inappropriateHeads")
    val offenseTypeList: List<OffenseType>? = null
)