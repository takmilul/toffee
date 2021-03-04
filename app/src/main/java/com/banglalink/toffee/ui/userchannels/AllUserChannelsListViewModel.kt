package com.banglalink.toffee.ui.userchannels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.banglalink.toffee.apiservice.AllUserChannelsService
import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.common.paging.BaseNetworkPagingSource
import com.banglalink.toffee.model.UserChannelInfo
import kotlinx.coroutines.flow.Flow

class AllUserChannelsListViewModel @ViewModelInject constructor(
    private val popularChannelApiService: AllUserChannelsService
) : ViewModel() {

    fun loadUserChannels(): Flow<PagingData<UserChannelInfo>> {
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