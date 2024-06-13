package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoveTokenizedAccountApiRequest(
    @SerialName("customerId") var customerId: Int? = null,
    @SerialName("password") var password: String? = null,
    @SerialName("is_prepaid") var isPrepaid: Int? = null,
    @SerialName("client_ip") var clientIp: String? = null,
    @SerialName("client_type") var clientType: String? = null,
    @SerialName("wallet_number") var walletNumber: String? = null,
    @SerialName("payment_token") var paymentToken: String? = null,
    @SerialName("payment_cus_id") var paymentCusId: String? = null
) : BaseRequest(ApiNames.REMOVE_TOKENIZE_ACCOUNT)