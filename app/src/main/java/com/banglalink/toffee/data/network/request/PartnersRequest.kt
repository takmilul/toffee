package com.banglalink.toffee.data.network.request

data class PartnersRequest (
    val customerId:Int,
    val password:String,
): BaseRequest("getUgcPartnerList")