package com.banglalink.toffee.data.network.response

import com.google.gson.annotations.SerializedName

data class RemoveTokenizeAccountApiBaseResponse (
    @SerializedName("response"    ) var response    : RemoveTokenizeAccountApiResponse? = RemoveTokenizeAccountApiResponse(),
): BaseResponse()

data class RemoveTokenizeAccountApiResponse (
    @SerializedName("status"  ) var status  : Boolean? = null,
    @SerializedName("message" ) var message : String?  = null
)