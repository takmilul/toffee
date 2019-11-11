package com.banglalink.toffee.data.network.request

data class UpdateProfileRequest(
    val fullname: String,
    val email: String,
    val phoneNo: String,
    val address:String,
    val customerId: Int,
    val password: String
):BaseRequest("subscriberProfileUpdate")