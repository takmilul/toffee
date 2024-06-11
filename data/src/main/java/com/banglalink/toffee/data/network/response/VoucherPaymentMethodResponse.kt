package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.VoucherPaymentBean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VoucherPaymentMethodResponse(
    @SerialName("response")
    val response: VoucherPaymentBean? = null
) : BaseResponse()