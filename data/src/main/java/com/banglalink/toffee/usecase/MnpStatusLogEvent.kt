package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.PubSubBaseRequest
import com.banglalink.toffee.notification.PAYMENT_LOG_FROM_DEVICE
import com.banglalink.toffee.notification.PubSubMessageUtil
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class MnpStatusLogEvent @Inject constructor() {
    
    fun execute(mnpStatusData: MnpStatusData) {
        PubSubMessageUtil.send(mnpStatusData, PAYMENT_LOG_FROM_DEVICE)
    }
}
@Serializable
data class MnpStatusData(
    @SerialName("callingApiName")
    val callingApiName  : String? = null,
    @SerialName("rawResponse")
    val rawResponse : String? = null,
) : PubSubBaseRequest()