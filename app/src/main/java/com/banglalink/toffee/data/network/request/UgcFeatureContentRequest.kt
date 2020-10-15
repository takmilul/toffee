package com.banglalink.toffee.data.network.request

data class UgcFeatureContentRequest(
    val customerId:Int,
    val password:String
) : BaseRequest("getUgcFeatureCategoryContents")