package com.banglalink.toffee.common.paging

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.Flow

abstract class BasePagingViewModel<T: Any>: ViewModel() {
    protected abstract val repo: BaseListRepository<T>// by lazy { BaseListRepositoryImpl(apiService) }
    open var enableToolbar = true

    val getListData: Flow<PagingData<T>> by lazy {
        repo
            .getList()
            .cachedIn(viewModelScope)
    }

    open fun onItemClicked(item: T) {}
}
