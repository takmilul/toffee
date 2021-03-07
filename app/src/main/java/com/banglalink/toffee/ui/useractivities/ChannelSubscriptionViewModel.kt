package com.banglalink.toffee.ui.useractivities

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.apiservice.GetChannelSubscriptions
import com.banglalink.toffee.common.paging.BaseListRepository
import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.common.paging.BaseNetworkPagingSource
import com.banglalink.toffee.common.paging.BasePagingViewModel
import com.banglalink.toffee.model.ChannelInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChannelSubscriptionViewModel @Inject constructor(
    apiService: GetChannelSubscriptions,
) : BasePagingViewModel<ChannelInfo>() {
    
    override val repo: BaseListRepository<ChannelInfo> by lazy {
        BaseListRepositoryImpl({ BaseNetworkPagingSource(apiService) })
    }

    val itemUpdateEvent = MutableLiveData<Int>()

    fun toggleNotification(item: ChannelInfo, pos: Int) {
        viewModelScope.launch {
//            item.apply { notificationStatus = !notificationStatus }
            itemUpdateEvent.value = pos
            delay(2000)
//            item.apply { notificationStatus = !notificationStatus }
            itemUpdateEvent.value = pos
        }
    }
}
