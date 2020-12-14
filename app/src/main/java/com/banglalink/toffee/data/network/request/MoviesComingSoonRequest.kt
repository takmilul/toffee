package com.banglalink.toffee.data.network.request

data class MoviesComingSoonRequest (
    val customerId:Int,
    val password:String,
): BaseRequest("getUgcComingSoon")