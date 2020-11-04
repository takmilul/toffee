package com.banglalink.toffee.common.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import java.lang.NullPointerException

class BaseRemoteMediator<T: Any>/*: RemoteMediator<Int, T>()*/ {
//    override suspend fun load(loadType: LoadType, state: PagingState<Int, T>): MediatorResult {
//        return MediatorResult.Error(NullPointerException())
//    }
}