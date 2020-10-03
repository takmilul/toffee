package com.banglalink.toffee.common.paging

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow

class BaseListRepositoryImpl<T: Any>(private val service: BaseApiService<T>): BaseListRepository<T> {
    override fun getList(): Flow<PagingData<T>> {
        val networkSource = BaseNetworkPagingSource(service)

        return Pager(
            config = PagingConfig(PAGE_SIZE, enablePlaceholders = true, initialLoadSize = PAGE_SIZE),
            pagingSourceFactory = {networkSource}
        ).flow
    }

    companion object {
        const val PAGE_SIZE = 10
    }
}