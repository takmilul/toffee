package com.banglalink.toffee.apiservice

interface BaseApiService<T: Any> {
    suspend fun loadData(offset: Int, limit: Int): List<T>
}