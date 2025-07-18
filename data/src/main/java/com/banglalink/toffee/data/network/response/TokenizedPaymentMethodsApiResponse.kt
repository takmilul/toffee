package com.banglalink.toffee.data.network.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TokenizedPaymentMethodsBaseApiResponse(
    @SerialName("response") var response: TokenizedPaymentMethodsApiResponse? = TokenizedPaymentMethodsApiResponse(),
) : BaseResponse()

@Serializable
data class TokenizedPaymentMethodsApiResponse(
    @SerialName("NAGAD") var nagadBean: NagadBean? = NagadBean()
)

@Serializable
data class NagadBean(
    @SerialName("payment_method_id") var paymentMethodId: Int? = 0,
    @SerialName("account") var nagadAccountInfo: NagadAccountInfo? = NagadAccountInfo()
)

@Serializable
data class NagadAccountInfo(
    @SerialName("payment_method_id") var paymentMethodId: Int? = 0,
    @SerialName("wallet_number") var walletNumber: String? = null,
    @SerialName("payment_token") var paymentToken: String? = null,
    @SerialName("token_expiry") var tokenExpiry: String? = null,
    @SerialName("payment_cus_id") var paymentCusId: String? = null
)