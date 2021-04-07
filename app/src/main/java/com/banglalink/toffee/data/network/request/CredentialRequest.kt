package com.banglalink.toffee.data.network.request

data class CredentialRequest (
        val parentId:Int=1,
        val fcmToken: String
        ) : BaseRequest("ugcCredentialByDeviceId")