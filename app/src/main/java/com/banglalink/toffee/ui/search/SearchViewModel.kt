package com.banglalink.toffee.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.banglalink.toffee.apiservice.ApiNames
import com.banglalink.toffee.apiservice.BrowsingScreens
import com.banglalink.toffee.apiservice.SearchContentService
import com.banglalink.toffee.common.paging.BaseListRepository
import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.common.paging.BaseNetworkPagingSource
import com.banglalink.toffee.common.paging.BasePagingViewModel
import com.banglalink.toffee.model.ChannelInfo
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

class SearchViewModel @AssistedInject constructor(
    private val searchApiService: SearchContentService.AssistedFactory,
    @Assisted private val keyword: String,
) : BasePagingViewModel<ChannelInfo>() {

    override fun repo(): BaseListRepository<ChannelInfo> {
        return BaseListRepositoryImpl({
            BaseNetworkPagingSource(searchApiService.create(keyword), ApiNames.GET_SEARCH_CONTENTS, BrowsingScreens.SEARCH_PAGE)
        })
    }

    @dagger.assisted.AssistedFactory
    interface AssistedFactory {
        fun create(keyword: String): SearchViewModel
    }

    companion object {
        fun provideFactory(assistedFactory: AssistedFactory, keyword: String): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return assistedFactory.create(keyword) as T
                }
            }
    }
}
