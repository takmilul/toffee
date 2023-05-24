package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.PubSubBaseRequest
import com.banglalink.toffee.notification.MNP_STATUS_LOG
import com.banglalink.toffee.notification.PubSubMessageUtil
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class MnpStatusLogEvent @Inject constructor() {
    private val gson = Gson()

    fun execute(mnpStatusData: MnpStatusData) {
        PubSubMessageUtil.sendMessage(gson.toJson(mnpStatusData), MNP_STATUS_LOG)
    }
}

data class MnpStatusData(
    @SerializedName("response")
    val response: Response?
) : PubSubBaseRequest()

data class Response (
    @SerializedName("mnp_status")
    val mnpStatus  : Int? = null,
    @SerializedName("api_name")
    val apiName  : String? = null,
    @SerializedName("is_bl_number")
    val isBlNumber : Boolean? = null,
    @SerializedName("is_prepaid")
    val isPrepaid  : Boolean? = null,
)