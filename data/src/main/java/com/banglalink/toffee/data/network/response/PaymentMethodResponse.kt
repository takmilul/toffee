package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.PaymentMethodBean
import com.google.gson.annotations.SerializedName

data class PaymentMethodResponse(
    @SerializedName("response")
    val response: PaymentMethodBean
) : BaseResponse()