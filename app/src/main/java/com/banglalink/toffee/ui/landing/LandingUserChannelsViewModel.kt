package com.banglalink.toffee.ui.landing

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.apiservice.MyChannelGetDetailService
import com.banglalink.toffee.apiservice.MyChannelSubscribeService
import com.banglalink.toffee.model.MyChannelSubscribeBean
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.util.getError
import kotlinx.coroutines.launch

class LandingUserChannelsViewModel @ViewModelInject constructor(
    private val subscribeApi: MyChannelSubscribeService,
    private val channelInfoApi: MyChannelGetDetailService
): ViewModel() {
    
    val subscriptionResponse = MutableLiveData<Resource<MyChannelSubscribeBean>>()
    val isChannelSubscribed = MutableLiveData<Boolean>()
    val channelSubscriberCount = MutableLiveData<String>()

    fun setSubscriptionStatus(channelId: Long, status: Int, channelOwnerId: Int) {
        viewModelScope.launch {
            try {
                val ret = subscribeApi(channelId.toInt(), status, channelOwnerId)
                subscriptionResponse.value = Resource.Success(ret)
            } catch (ex: Exception) {
                ex.printStackTrace()
                subscriptionResponse.value = Resource.Failure(getError(ex))
            }
        }
    }

    fun getChannelInfo(isOwner: Int, isPublic: Int, channelId: Long, channelOwnerId: Int) {
        viewModelScope.launch {
            try {
                val ret = channelInfoApi.execute(isOwner, isPublic, channelId.toInt(), channelOwnerId)
                isChannelSubscribed.value = ret.isSubscribed == 1
                channelSubscriberCount.value = ret.formattedSubscriberCount
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    fun toggleSubscriptionStatus(channelId: Int, channelOwnerId: Int) {
        val currentStatus = isChannelSubscribed.value ?: return
        viewModelScope.launch {
            try {
                val newStatus = if(currentStatus) 0 else 1
                val ret = subscribeApi(channelId, newStatus, channelOwnerId)
                isChannelSubscribed.value = ret.isSubscribed == 1
            }catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }
}