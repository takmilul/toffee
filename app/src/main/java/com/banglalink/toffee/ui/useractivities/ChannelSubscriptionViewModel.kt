package com.banglalink.toffee.ui.useractivities

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.data.network.retrofit.RetrofitApiClient
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.ChannelSubscriptionInfo
import com.banglalink.toffee.ui.common.SingleListRepository
import com.banglalink.toffee.ui.common.SingleListViewModel
import com.banglalink.toffee.usecase.GetChannelSubscriptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ChannelSubscriptionViewModel: SingleListViewModel<ChannelSubscriptionInfo>() {
    override var repo: SingleListRepository<ChannelSubscriptionInfo>  = GetChannelSubscriptions(Preference.getInstance(), RetrofitApiClient.toffeeApi)

    val itemUpdateEvent = MutableLiveData<Int>()

    fun toggleNotification(item: ChannelSubscriptionInfo, pos: Int) {
        viewModelScope.launch {
            item.apply { notificationStatus = !notificationStatus }
            itemUpdateEvent.value = pos
            delay(2000)
            item.apply { notificationStatus = !notificationStatus }
            itemUpdateEvent.value = pos
        }
    }
}
