package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.PaymentMethodBean
import com.banglalink.toffee.model.VoucherPaymentBean
import com.google.gson.annotations.SerializedName

data class VoucherPaymentMethodResponse(
    @SerializedName("response")
    val response: VoucherPaymentBean
) : BaseResponse()