package com.banglalink.toffee.data.network.response

import com.google.gson.annotations.SerializedName

data class TokenizedAccountInfoApiResponse(
    @SerializedName("response"    ) var response    : List<TokenizedAccountInfo> = listOf(),
    ) : BaseResponse()
data class TokenizedAccountInfo (

    @SerializedName("payment_method_id" ) var paymentMethodId : Int?    = null,
    @SerializedName("wallet_number"     ) var walletNumber    : String? = null,
    @SerializedName("payment_token"     ) var paymentToken    : String? = null,
    @SerializedName("token_expiry"      ) var tokenExpiry     : String? = null,
    @SerializedName("payment_cus_id"    ) var paymentCusId    : String? = null
)