package com.banglalink.toffee.data.network.request

data class SearchContentRequest(
    val keyword: String,
    val customerId: Int,
    val password: String,
    val offset: Int,
    val limit: Int
):BaseRequest("getSearchContents")