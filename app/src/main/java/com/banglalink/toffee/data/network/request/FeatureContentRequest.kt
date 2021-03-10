package com.banglalink.toffee.data.network.request

data class FeatureContentRequest(
    val customerId:Int,
    val password:String
) : BaseRequest("getUgcFeatureCategoryContents")