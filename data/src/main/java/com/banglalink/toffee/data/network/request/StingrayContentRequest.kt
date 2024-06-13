package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StingrayContentRequest(
    @SerialName("customerId")
    val customerId: Int,
    @SerialName("password")
    val password: String,
    @SerialName("telcoId")
    val telcoId: Int = 1,
    @SerialName("type")
    val type:String ="stingray"
) : BaseRequest(ApiNames.GET_STINGRAY_CONTENTS)