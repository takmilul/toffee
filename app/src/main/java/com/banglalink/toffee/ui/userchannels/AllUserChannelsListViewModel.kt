package com.banglalink.toffee.ui.userchannels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.banglalink.toffee.apiservice.AllUserChannelsService
import com.banglalink.toffee.apiservice.ApiNames
import com.banglalink.toffee.apiservice.BrowsingScreens
import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.common.paging.BaseNetworkPagingSource
import com.banglalink.toffee.model.UserChannelInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class AllUserChannelsListViewModel @Inject constructor(
    private val popularChannelApiService: AllUserChannelsService,
) : ViewModel() {
    
    fun loadUserChannels(initialPage: Int = 0): Flow<PagingData<UserChannelInfo>> {
        return BaseListRepositoryImpl({
            BaseNetworkPagingSource(
                popularChannelApiService, ApiNames.GET_ALL_USER_CHANNEL, BrowsingScreens.ALL_USER_CHANNELS_PAGE, initialPage
            )
        }).getList().cachedIn(viewModelScope)
    }
}