package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.ProfileBean
import com.google.gson.annotations.SerializedName

data class ProfileResponse(
    @SerializedName("response")
    val response: ProfileBean
) : BaseResponse()