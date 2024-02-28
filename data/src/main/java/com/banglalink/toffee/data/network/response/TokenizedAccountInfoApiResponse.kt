package com.banglalink.toffee.data.network.response

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TokenizedAccountInfoApiResponse(
    @SerialName("response"    ) var response    : List<TokenizedAccountInfo> = listOf(),
    ) : BaseResponse()
@Serializable
data class TokenizedAccountInfo (

    @SerialName("payment_method_id" ) var paymentMethodId : Int?    = null,
    @SerialName("wallet_number"     ) var walletNumber    : String? = null,
    @SerialName("payment_token"     ) var paymentToken    : String? = null,
    @SerialName("token_expiry"      ) var tokenExpiry     : String? = null,
    @SerialName("payment_cus_id"    ) var paymentCusId    : String? = null
)