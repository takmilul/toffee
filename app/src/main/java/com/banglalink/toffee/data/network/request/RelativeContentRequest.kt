package com.banglalink.toffee.data.network.request

data class RelativeContentRequest(
    val contentId: String,
    val videoTag: String,
    val customerId: Int,
    val password: String,
    val categoryId: Int,
    val subCategoryId: Int,
    override val offset: Int,
    override val limit: Int
):BasePagingRequest("getUgcRelativeContentsExt")