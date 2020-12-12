package com.banglalink.toffee.data.network.request

data class MovieCategoryDetailRequest (
    val customerId:Int,
    val password:String,
): BaseRequest("getUgcMovieCategoryDetails")