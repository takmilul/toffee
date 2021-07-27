package com.banglalink.toffee.data.network.request

data class VastTagRequest(
    val customerId:Int,
    val password: String
) : BaseRequest("vastTagsList")