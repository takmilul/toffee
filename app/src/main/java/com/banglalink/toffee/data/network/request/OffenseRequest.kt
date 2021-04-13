package com.banglalink.toffee.data.network.request

data class OffenseRequest(
    val customerId:Int,
    val password:String,
) : BaseRequest("getUgcInappropriateHeadList")