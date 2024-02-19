package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import com.google.gson.annotations.SerializedName

data class RemoveTokenizedAccountApiRequest(
    @SerializedName("customerId") var customerId: Int? = null,
    @SerializedName("password") var password: String? = null,
    @SerializedName("is_prepaid") var isPrepaid: Int? = null,
    @SerializedName("client_ip") var clientIp: String? = null,
    @SerializedName("client_type") var clientType: String? = null,
    @SerializedName("wallet_number") var walletNumber: String? = null,
    @SerializedName("payment_token") var paymentToken: String? = null,
    @SerializedName("payment_cus_id") var paymentCusId: String? = null
) : BaseRequest(ApiNames.REMOVE_TOKENIZE_ACCOUNT)