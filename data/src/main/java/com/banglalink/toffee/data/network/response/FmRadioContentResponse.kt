package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.ContentBean
import com.banglalink.toffee.model.FmRadioContentBean
import com.google.gson.annotations.SerializedName

data class FmRadioContentResponse (
    @SerializedName("response")
    val response: FmRadioContentBean
) : BaseResponse()