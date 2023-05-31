package com.banglalink.toffee.common.paging

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow

class BaseListRepositoryImpl<T: Any> constructor(
    private val pagingFactory: ()-> PagingSource<Int, T>,
    private val remoteMediator: BaseRemoteMediator<T>? = null
): BaseListRepository<T> {
    override fun getList(pageSize: Int): Flow<PagingData<T>> {
        val finalPageSize = if (pageSize <= 0) 30 else pageSize
        return Pager(
            config = PagingConfig(
                finalPageSize,
                enablePlaceholders = true,
                initialLoadSize = finalPageSize,
                prefetchDistance = if(finalPageSize > 30) finalPageSize / 2 else 10,
//                maxSize = 2 * PAGE_SIZE
            ),
//            remoteMediator = remoteMediator,
            pagingSourceFactory = pagingFactory
        ).flow
    }
    
    companion object {
        const val PAGE_SIZE = 30
    }
}