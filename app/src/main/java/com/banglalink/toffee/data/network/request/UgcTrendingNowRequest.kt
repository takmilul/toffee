package com.banglalink.toffee.data.network.request

data class UgcTrendingNowRequest(
    val customerId:Int,
    val password:String
) : BaseRequest("getUgcCategoryEditorChoice")