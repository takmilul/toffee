package com.banglalink.toffee.data.network.request

import com.google.gson.annotations.SerializedName

data class CredentialRequest(
    @SerializedName("parentId")
    val parentId: Int = 1,
    @SerializedName("fcmToken")
    val fcmToken: String,
    @SerializedName("msisdn")
    val msisdn: String = "",
) : BaseRequest("ugcCredentialByDeviceId") {
    @get:SerializedName("isBlNumber")
    override var isBlNumber: String
        get() = super.isBlNumber
        set(value) {
            super.isBlNumber = value
        }
}