package com.banglalink.toffee.ui.subscription

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.banglalink.toffee.apiservice.ApiNames
import com.banglalink.toffee.apiservice.BrowsingScreens
import com.banglalink.toffee.apiservice.SubscribedUserChannelsService
import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.common.paging.BaseNetworkPagingSource
import com.banglalink.toffee.data.network.util.resultFromResponse
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.model.UserChannelInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubscribedChannelsViewModel @Inject constructor(
    private val subscribeChannelApiService: SubscribedUserChannelsService,
) : ViewModel() {

    val subscribedChannelLiveData = MutableLiveData<Boolean>()
    
    fun syncSubscribedChannels() {
        viewModelScope.launch {
            val result = resultFromResponse { subscribeChannelApiService.loadData(0, 100) }
            subscribedChannelLiveData.postValue(result is Success)
        }
    }
    
    fun loadSubscribedChannels(): Flow<PagingData<UserChannelInfo>> {
        return userChannelRepo.getList()
    }
    
    private val userChannelRepo by lazy {
        BaseListRepositoryImpl({
            BaseNetworkPagingSource(
                subscribeChannelApiService, ApiNames.GET_SUBSCRIBED_USER_CHANNEL, BrowsingScreens.SUBSCRIBED_CHANNELS_PAGE
            )
        })
    }
}