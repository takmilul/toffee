package com.banglalink.toffee.common.paging

interface BaseApiService<T: Any> {
    suspend fun loadData(offset: Int, limit: Int): List<T>
}