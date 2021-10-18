package com.banglalink.toffee.data.network.request

data class UploadProfileImageRequest(val profilePhoto: String,
                                     val customerId: Int, val password: String, val isDeletePhoto: Boolean = false) :
    BaseRequest("subscriberProfilePhoto")