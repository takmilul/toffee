package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.PubSubBaseRequest
import com.banglalink.toffee.notification.PAYMENT_LOG_FROM_DEVICE
import com.banglalink.toffee.notification.PubSubMessageUtil
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class MnpStatusLogEvent @Inject constructor() {
    
    fun execute(mnpStatusData: MnpStatusData) {
        PubSubMessageUtil.send(mnpStatusData, PAYMENT_LOG_FROM_DEVICE)
    }
}

data class MnpStatusData(
    @SerializedName("callingApiName")
    val callingApiName  : String? = null,
    @SerializedName("rawResponse")
    val rawResponse : String? = null,
) : PubSubBaseRequest()