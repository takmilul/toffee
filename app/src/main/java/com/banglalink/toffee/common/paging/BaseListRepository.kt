package com.banglalink.toffee.common.paging

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow

interface BaseListRepository<T: Any> {
    fun getList(): Flow<PagingData<T>>
}