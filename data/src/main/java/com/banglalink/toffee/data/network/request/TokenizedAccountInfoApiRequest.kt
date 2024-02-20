package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import com.google.gson.annotations.SerializedName

data class TokenizedAccountInfoApiRequest(
    @SerializedName("customerId"      ) var customerId      : Int?    = null,
    @SerializedName("password"        ) var password        : String? = null,
): BaseRequest(ApiNames.TOKENIZED_ACCOUNT_INFO)
