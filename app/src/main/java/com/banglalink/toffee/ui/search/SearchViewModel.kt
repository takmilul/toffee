package com.banglalink.toffee.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.banglalink.toffee.apiservice.SearchContentService
import com.banglalink.toffee.common.paging.BaseListRepository
import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.common.paging.BaseNetworkPagingSource
import com.banglalink.toffee.common.paging.BasePagingViewModel
import com.banglalink.toffee.model.ChannelInfo
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject

class SearchViewModel @AssistedInject constructor(
    private val searchApiService: SearchContentService.AssistedFactory,
    @Assisted private val keyword: String
) : BasePagingViewModel<ChannelInfo>()  {

    override val repo: BaseListRepository<ChannelInfo> by lazy {
        BaseListRepositoryImpl({
            BaseNetworkPagingSource(searchApiService.create(keyword))
        })
    }

    @AssistedInject.Factory
    interface AssistedFactory {
        fun create(keyword: String): SearchViewModel
    }

    companion object {
        fun provideFactory(assistedFactory: AssistedFactory, keyword: String): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                    return assistedFactory.create(keyword) as T
                }
            }
    }
}
