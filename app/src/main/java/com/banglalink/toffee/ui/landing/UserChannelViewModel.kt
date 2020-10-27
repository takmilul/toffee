package com.banglalink.toffee.ui.landing

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.apiservice.MyChannelGetDetailService
import com.banglalink.toffee.apiservice.MyChannelSubscribeService
import com.banglalink.toffee.model.MyChannelSubscribeBean
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.model.UgcUserChannelInfo
import com.banglalink.toffee.util.getError
import kotlinx.coroutines.launch

class UserChannelViewModel @ViewModelInject constructor(
    private val subscribeApi: MyChannelSubscribeService,
    private val channelInfoApi: MyChannelGetDetailService
): ViewModel() {
    val subscriptionResponse = MutableLiveData<Resource<MyChannelSubscribeBean>>()
    val isChannelSubscribed = MutableLiveData<Boolean>()
    val channelSubscriberCount = MutableLiveData<String>()

    fun setSubscriptionStatus(channelId: Long, status: Int) {
        viewModelScope.launch {
            try {
                val ret = subscribeApi(channelId.toInt(), status)
                subscriptionResponse.value = Resource.Success(ret)
            } catch (ex: Exception) {
                ex.printStackTrace()
                subscriptionResponse.value = Resource.Failure(getError(ex))
            }
        }
    }

    fun getChannelInfo(channdlId: Long, isOwner: Int) {
        viewModelScope.launch {
            try {
                val ret = channelInfoApi.execute(isOwner, channdlId.toInt())
                isChannelSubscribed.value = ret.isSubscribed == 1
                channelSubscriberCount.value = ret.formattedSubscriberCount
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    fun toggleSubscriptionStatus(channelId: Long) {
        val currentStatus = isChannelSubscribed.value ?: return
        viewModelScope.launch {
            try {
                val newStatus = 1 - (if(currentStatus) 1 else 0)
                val ret = subscribeApi(channelId.toInt(), newStatus)
                isChannelSubscribed.value = ret.isSubscribed == 1
            }catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }
}