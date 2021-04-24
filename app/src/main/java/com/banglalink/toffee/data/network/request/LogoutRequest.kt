package com.banglalink.toffee.data.network.request

data class LogoutRequest(
    val customerId:Int,
    val password:String,
) : BaseRequest("ugcUserUnverified")