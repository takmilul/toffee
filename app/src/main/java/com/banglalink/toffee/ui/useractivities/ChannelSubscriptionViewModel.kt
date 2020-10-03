package com.banglalink.toffee.ui.useractivities

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.common.paging.BasePagingViewModel
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.apiservice.GetChannelSubscriptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ChannelSubscriptionViewModel @ViewModelInject constructor(
        override val apiService: GetChannelSubscriptions
)
    :BasePagingViewModel<ChannelInfo>() {

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
