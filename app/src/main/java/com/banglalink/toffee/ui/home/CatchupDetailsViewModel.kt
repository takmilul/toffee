package com.banglalink.toffee.ui.home

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.banglalink.toffee.apiservice.CatchupParams
import com.banglalink.toffee.apiservice.GetRelativeContents
import com.banglalink.toffee.apiservice.MyChannelGetDetailService
import com.banglalink.toffee.apiservice.MyChannelSubscribeService
import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.common.paging.BaseNetworkPagingSource
import com.banglalink.toffee.data.network.retrofit.CacheManager
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.MyChannelSubscribeBean
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseViewModel
import com.banglalink.toffee.util.getError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class CatchupDetailsViewModel @ViewModelInject constructor(
    private val subscribeApi: MyChannelSubscribeService,
    private val channelInfoApi: MyChannelGetDetailService,
    private val cacheManager: CacheManager,
    private val relativeContentsFactory: GetRelativeContents.AssistedFactory
):BaseViewModel() {
    val subscriptionResponse = MutableLiveData<Resource<MyChannelSubscribeBean>>()
    val isChannelSubscribed = MutableLiveData<Boolean>()
    val channelSubscriberCount = MutableLiveData<Int>()

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
                cacheManager.clearSubscriptionCache()
                isChannelSubscribed.value = ret.isSubscribed == 1
                channelSubscriberCount.value = ret.subscriberCount
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
                channelSubscriberCount.value = ret.subscriberCount
            }catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }
    fun loadRelativeContent(channelInfo: ChannelInfo): Flow<PagingData<ChannelInfo>> {
        return BaseListRepositoryImpl({
            BaseNetworkPagingSource(
                relativeContentsFactory.create(
                    CatchupParams(channelInfo.id, channelInfo.video_tags)
                )
            )
        }).getList()
    }
}