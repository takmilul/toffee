package com.banglalink.toffee.data.network.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoveTokenizeAccountApiBaseResponse (
    @SerialName("response"    ) var response    : RemoveTokenizeAccountApiResponse? = RemoveTokenizeAccountApiResponse(),
): BaseResponse()
@Serializable
data class RemoveTokenizeAccountApiResponse (
    @SerialName("status"  ) var status  : Boolean? = null,
    @SerialName("message" ) var message : String?  = null
)