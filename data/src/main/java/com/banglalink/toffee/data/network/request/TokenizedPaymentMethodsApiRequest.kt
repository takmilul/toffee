package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TokenizedPaymentMethodsApiRequest(
    @SerialName("customerId"      ) var customerId      : Int?    = null,
    @SerialName("password"        ) var password        : String? = null,
): BaseRequest(ApiNames.TOKENIZED_PAYMENT_METHODS)
