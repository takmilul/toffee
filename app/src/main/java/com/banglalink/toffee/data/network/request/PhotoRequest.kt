package com.banglalink.toffee.data.network.request

data class PhotoRequest(val profilePhoto: String, val customerId: String, val password: String) :
    BaseRequest("subscriberProfilePhoto")