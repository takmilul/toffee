package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.ProfileResponseBean
import com.google.gson.annotations.SerializedName

data class UpdateProfileResponse(
    @SerializedName("response")
    val response: ProfileResponseBean
):BaseResponse()