package com.banglalink.toffee.data.network.response

import com.google.gson.annotations.SerializedName

data class MnpStatusResponse(
    @SerializedName("response")
    val response: MnpStatusBean?
) : BaseResponse()

data class MnpStatusBean (
    @SerializedName("mnp_status")
    val mnpStatus  : Int? = null,
    @SerializedName("is_bl_number")
    val isBlNumber : Boolean? = null,
    @SerializedName("is_prepaid")
    val isPrepaid  : Boolean? = null
)