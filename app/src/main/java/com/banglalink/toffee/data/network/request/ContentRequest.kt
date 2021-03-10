package com.banglalink.toffee.data.network.request

data class ContentRequest(
    val categoryId: Int,
    val subCategoryId: Int,
    val type:String,
    val customerId:Int,
    val password:String,
    val telcoId: Int = 1,
    override val offset:Int,
    override val limit:Int=10
) : BasePagingRequest("getUgcContentsV5")