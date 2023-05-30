package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.PubSubBaseRequest
import com.banglalink.toffee.notification.PAYMENT_LOG_FROM_DEVICE
import com.banglalink.toffee.notification.PubSubMessageUtil
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class MnpStatusLogEvent @Inject constructor() {
    private val gson = Gson()

    fun execute(mnpStatusData: MnpStatusData) {
        PubSubMessageUtil.sendMessage(gson.toJson(mnpStatusData), PAYMENT_LOG_FROM_DEVICE)
    }
}

data class MnpStatusData(
    @SerializedName("mnp_status")
    val mnpStatus  : Int? = null,
    @SerializedName("api_name")
    val apiName  : String? = null,
    @SerializedName("rawResponse")
    val rawResponse : String? = null,
) : PubSubBaseRequest()