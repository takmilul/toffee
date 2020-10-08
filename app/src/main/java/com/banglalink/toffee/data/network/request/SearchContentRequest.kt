package com.banglalink.toffee.data.network.request

data class SearchContentRequest(
    val keyword: String,
    val customerId: Int,
    val password: String,
    override val offset: Int,
    override val limit: Int
):BasePagingRequest("getSearchContents")