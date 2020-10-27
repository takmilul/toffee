package com.banglalink.toffee.ui.landing

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.apiservice.MyChannelSubscribeService
import com.banglalink.toffee.model.MyChannelSubscribeBean
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.model.UgcUserChannelInfo
import com.banglalink.toffee.util.getError
import kotlinx.coroutines.launch

class UserChannelViewModel @ViewModelInject constructor(
    private val subscribeApi: MyChannelSubscribeService
): ViewModel() {
    val subscriptionResponse = MutableLiveData<Resource<MyChannelSubscribeBean>>()

    fun setSubscriptionStatus(channelInfo: UgcUserChannelInfo, status: Int) {
        viewModelScope.launch {
            try {
                val ret = subscribeApi(channelInfo.id.toInt(), status)
                subscriptionResponse.value = Resource.Success(ret)
            } catch (ex: Exception) {
                ex.printStackTrace()
                subscriptionResponse.value = Resource.Failure(getError(ex))
            }
        }
    }
}