package com.banglalink.toffee.data.network.request

data class AllChannelRequest(
    val subCategoryId: Int,
    val customerId:Int,
    val password:String,
    val telcoId: Int = 1,
    val limit:Int=1
) : BaseRequest("getAppHomePageContentTofee")