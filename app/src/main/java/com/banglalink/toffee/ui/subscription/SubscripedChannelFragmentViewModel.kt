package com.banglalink.toffee.ui.subscription

import androidx.lifecycle.ViewModel
import androidx.paging.PagingData
import com.banglalink.toffee.apiservice.SubscribedUserChannelsService
import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.common.paging.BaseNetworkPagingSource
import com.banglalink.toffee.model.UserChannelInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class SubscribedChannelFragmentViewModel @Inject constructor(
    private val subscribeChannelApiService: SubscribedUserChannelsService,
) : ViewModel() {

    fun loadUserChannels(): Flow<PagingData<UserChannelInfo>> {
        return userChannelRepo.getList()
    }
    
    private val userChannelRepo by lazy {
        BaseListRepositoryImpl({
            BaseNetworkPagingSource(
                subscribeChannelApiService
            )
        })
    }
}