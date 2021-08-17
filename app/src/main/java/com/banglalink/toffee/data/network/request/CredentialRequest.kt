package com.banglalink.toffee.data.network.request

data class CredentialRequest(
    val parentId: Int = 1,
    val fcmToken: String,
    val msisdn: String = "",
) : BaseRequest("ugcCredentialByDeviceId") {
    override var isBlNumber: String 
        get() = super.isBlNumber
        set(value) {super.isBlNumber = value}
}