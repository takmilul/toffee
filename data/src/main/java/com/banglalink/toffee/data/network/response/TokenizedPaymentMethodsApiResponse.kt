package com.banglalink.toffee.data.network.response

import com.google.gson.annotations.SerializedName

data class TokenizedPaymentMethodsBaseApiResponse(
    @SerializedName("response") var response: TokenizedPaymentMethodsApiResponse? = TokenizedPaymentMethodsApiResponse(),
) : BaseResponse()

data class TokenizedPaymentMethodsApiResponse(
    @SerializedName("NAGAD") var nagadBean: NagadBean? = NagadBean()
)

data class NagadBean(
    @SerializedName("payment_method_id") var paymentMethodId: Int? = null,
    @SerializedName("account") var nagadAccountInfo: NagadAccountInfo? = NagadAccountInfo()
)

data class NagadAccountInfo(
    @SerializedName("payment_method_id") var paymentMethodId: Int? = null,
    @SerializedName("wallet_number") var walletNumber: String? = null,
    @SerializedName("payment_token") var paymentToken: String? = null,
    @SerializedName("token_expiry") var tokenExpiry: String? = null,
    @SerializedName("payment_cus_id") var paymentCusId: String? = null
)