package com.banglalink.toffee.common.paging

import androidx.paging.*
import kotlinx.coroutines.flow.Flow

class BaseDBListRepositoryImpl<T: Any> constructor(
    private val pagingSourceFactory: () -> PagingSource<Int, T>,
    private val remoteMediator: BaseRemoteMediator<T>? = null
): BaseListRepository<T> {
    override fun getList(): Flow<PagingData<T>> {
        return Pager(
            config = PagingConfig(PAGE_SIZE, enablePlaceholders = false, initialLoadSize = PAGE_SIZE * 2),
//            remoteMediator = remoteMediator,
            pagingSourceFactory = pagingSourceFactory
        ).flow
    }

    companion object {
        const val PAGE_SIZE = 20
    }
}