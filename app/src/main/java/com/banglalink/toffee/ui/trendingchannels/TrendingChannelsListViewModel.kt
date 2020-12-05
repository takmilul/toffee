package com.banglalink.toffee.ui.trendingchannels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.banglalink.toffee.apiservice.ApiCategoryRequestParams
import com.banglalink.toffee.apiservice.TrendingChannelsService
import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.common.paging.BaseNetworkPagingSource
import com.banglalink.toffee.model.TrendingChannelInfo
import com.banglalink.toffee.model.UgcUserChannelInfo
import kotlinx.coroutines.flow.Flow

class TrendingChannelsListViewModel @ViewModelInject constructor (
        private val popularChannelApiService: TrendingChannelsService
    ) : ViewModel() {

    fun loadUserChannels(): Flow<PagingData<TrendingChannelInfo>> {
        return userChannelRepo.getList().cachedIn(viewModelScope)
    }

    private val userChannelRepo by lazy {
        BaseListRepositoryImpl({
            BaseNetworkPagingSource(
                popularChannelApiService
            )
        })
    }
}