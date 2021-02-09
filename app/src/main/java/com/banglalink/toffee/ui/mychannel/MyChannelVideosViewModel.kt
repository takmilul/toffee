package com.banglalink.toffee.ui.mychannel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.apiservice.MyChannelVideoDeleteService
import com.banglalink.toffee.apiservice.MyChannelVideosRequestParams
import com.banglalink.toffee.apiservice.MyChannelVideosService
import com.banglalink.toffee.common.paging.BaseListRepository
import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.common.paging.BaseNetworkPagingSource
import com.banglalink.toffee.common.paging.BasePagingViewModel
import com.banglalink.toffee.data.network.util.resultFromResponse
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.MyChannelDeleteVideoBean
import com.banglalink.toffee.model.Resource
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.launch

class MyChannelVideosViewModel @AssistedInject constructor(
    private val getMyChannelAssistedFactory: MyChannelVideosService.AssistedFactory,
    private val myChannelVideoDeleteApiService: MyChannelVideoDeleteService,
    @Assisted private val isOwner: Int, 
    @Assisted private val channelOwnerId: Int,
    @Assisted private val isPublic: Int) : BasePagingViewModel<ChannelInfo>() {

    private val _data = MutableLiveData<Resource<MyChannelDeleteVideoBean>>()
    val deleteVideoLiveData = _data.toLiveData()
    
    override val repo: BaseListRepository<ChannelInfo> by lazy {
        BaseListRepositoryImpl({BaseNetworkPagingSource(getMyChannelAssistedFactory.create(MyChannelVideosRequestParams("VOD", isOwner, channelOwnerId, 0, 0, isPublic)))})
    }

    @AssistedInject.Factory
    interface AssistedFactory {
        fun create(isOwner: Int, channelOwnerId: Int, isPublic: Int): MyChannelVideosViewModel
    }

    companion object {
        fun provideFactory(
            assistedFactory: AssistedFactory,
            isOwner: Int, channelOwnerId: Int, isPublic: Int
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return assistedFactory.create(isOwner, channelOwnerId, isPublic) as T
            }
        }
    }

    fun deleteVideo(contentId: Int){
        viewModelScope.launch { 
            _data.postValue(resultFromResponse { myChannelVideoDeleteApiService.invoke(contentId) })
        }
    }
    
    /*fun getReactionByContent(contentId: String): ReactionInfo? {
        var reactionInfo: ReactionInfo? = null
        viewModelScope.launch {
            reactionInfo = async { reactionDao.getReactionByContentId(contentId)}.await()
        }
        return reactionInfo
    }*/

    /*fun insert(reactionInfo: ReactionInfo) {
        viewModelScope.launch {
            reactionDao.insert(reactionInfo)
        }
    }

    fun insertActivity(channelInfo: ChannelInfo, reactStatus: Int) {
        viewModelScope.launch {
            val item = UserActivities(
                preference.customerId,
                channelInfo.id.toLong(),
                "activity",
                channelInfo.type ?: "VOD",
                Gson().toJson(channelInfo),
                ActivityType.REACT.value,
                reactStatus
            )
            activitiesRepo.insert(item)
        }
    }*/
}