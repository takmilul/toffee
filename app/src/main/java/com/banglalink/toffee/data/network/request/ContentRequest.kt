package com.banglalink.toffee.data.network.request

data class ContentRequest(
    val categoryId: Int,
    val subCategoryId: Int,
    val type:String,
    val customerId:Int,
    val password:String,
    val telcoId: Int = 1,
    val offset:Int,
    val limit:Int=10
) : BaseRequest("getContentsV2")