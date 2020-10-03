package com.banglalink.toffee.common.paging

import androidx.paging.PagingSource

class BaseNetworkPagingSource<T: Any>(private val service: BaseApiService<T>): PagingSource<Int, T>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        val page = params.key ?: 0
        return try {
            val resp = service.loadData(page * params.loadSize, params.loadSize)
            LoadResult.Page(
                data = resp,
                prevKey = if(page == 0) null else page - 1,
                nextKey = if(resp.isEmpty()) null else page + 1
            )
        } catch (ex: Exception) {
            LoadResult.Error(ex)
        }
    }
}