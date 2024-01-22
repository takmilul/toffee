package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.PaymentMethodBean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaymentMethodResponse(
    @SerialName("response")
    val response: PaymentMethodBean
) : BaseResponse()