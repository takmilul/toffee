package com.banglalink.toffee.common.paging

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.Flow

abstract class BasePagingViewModel<T: Any>: ViewModel() {
    protected abstract val repo: BaseListRepository<T>// by lazy { BaseListRepositoryImpl(apiService) }
    open var enableToolbar = true

    fun getListData(): Flow<PagingData<T>> {
        return repo
                .getList()
                .cachedIn(viewModelScope)
    }

    open fun onItemClicked(item: T) {}
}
