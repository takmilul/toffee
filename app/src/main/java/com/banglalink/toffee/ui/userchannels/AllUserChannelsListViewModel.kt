package com.banglalink.toffee.ui.userchannels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.banglalink.toffee.apiservice.AllUserChannelsService
import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.common.paging.BaseNetworkPagingSource
import com.banglalink.toffee.data.database.entities.SubscriptionCount
import com.banglalink.toffee.model.UserChannelInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class AllUserChannelsListViewModel @Inject constructor(
        private val popularChannelApiService: AllUserChannelsService,
          //  private val subscriptionCountRepository: SubscriptionCountRepository
) : ViewModel() {
    var subscriptionCount: SubscriptionCount? =null
    fun loadUserChannels(): Flow<PagingData<UserChannelInfo>> {
        return userChannelRepo.getList().cachedIn(viewModelScope)
    }

//    fun updateSubscriptionInfoDb(channelId: Int, status: Int){
//        viewModelScope.launch{
//            subscriptionCount=subscriptionCountRepository.getSubscriptionCount(channelId)
//             if (subscriptionCount!=null){
//              subscriptionCountRepository.updateSubscriptionCount(channelId, status)
//               }
//           else{
//             subscriptionCountRepository.insert(SubscriptionCount(channelId, 1))
//              }
//                 }
//         }
    private val userChannelRepo by lazy {
        BaseListRepositoryImpl({
            BaseNetworkPagingSource(
                popularChannelApiService
            )
        })
    }
}