package com.banglalink.toffee.ui.useractivities

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.common.paging.BasePagingViewModel
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.apiservice.GetChannelSubscriptions
import com.banglalink.toffee.common.paging.BaseListRepository
import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.common.paging.BaseNetworkPagingSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ChannelSubscriptionViewModel @ViewModelInject constructor(
        apiService: GetChannelSubscriptions
)
    :BasePagingViewModel<ChannelInfo>() {
    override val repo: BaseListRepository<ChannelInfo> by lazy {
        BaseListRepositoryImpl(BaseNetworkPagingSource(apiService))
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
