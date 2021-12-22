package com.banglalink.toffee.common.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.banglalink.toffee.apiservice.BaseApiService
import com.banglalink.toffee.util.getError

class BaseNetworkPagingSource<T: Any>(
    private val service: BaseApiService<T>,
    private val apiName: String,
    private val screenName: String,
    private val initialPage: Int = 0
): 
    PagingSource<Int, T>() {
    override fun getRefreshKey(state: PagingState<Int, T>): Int? = null
    
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        val page = params.key ?: initialPage
        return try {
            val resp = service.loadData(page * params.loadSize, params.loadSize)
            LoadResult.Page(
                data = resp,
                prevKey = if(page == 0) null else page - 1,
                nextKey = if(resp.size < params.loadSize) null else page + 1
            )
        } catch (ex: Exception) {
            val error = getError(ex)
            ex.printStackTrace()
            LoadResult.Error(ex)
        }
    }
}