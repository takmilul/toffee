package com.banglalink.toffee.data.network.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CredentialRequest(
    @SerialName("parentId")
    val parentId: Int = 1,
    @SerialName("fcmToken")
    val fcmToken: String,
    @SerialName("msisdn")
    val msisdn: String = "",
) : BaseRequest("ugcCredentialByDeviceId") {
//    @get:SerializedName("isBlNumber")
    override var isBlNumber: String
        get() = super.isBlNumber
        set(value) {
            super.isBlNumber = value
        }
}