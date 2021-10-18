package com.banglalink.toffee.data.network.request

data class SubCategoryRequest (
    val customerId:Int,
    val password:String
): BaseRequest("getUgcSubCategories")
