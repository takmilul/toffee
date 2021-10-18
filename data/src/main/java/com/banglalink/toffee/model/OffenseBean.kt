package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class OffenseBean(
    @SerializedName("inappropriateHeads")
    val offenseTypeList: List<OffenseType>? = null
)