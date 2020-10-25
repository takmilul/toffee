package com.banglalink.toffee.ui.mychannel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.apiservice.MyChannelVideosRequestParams
import com.banglalink.toffee.apiservice.MyChannelVideosService
import com.banglalink.toffee.common.paging.BaseListRepository
import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.common.paging.BaseNetworkPagingSource
import com.banglalink.toffee.common.paging.BasePagingViewModel
import com.banglalink.toffee.data.database.dao.ReactionDao
import com.banglalink.toffee.data.database.entities.ReactionInfo
import com.banglalink.toffee.data.database.entities.UserActivities
import com.banglalink.toffee.data.repository.UserActivitiesRepository
import com.banglalink.toffee.enums.ActivityType
import com.banglalink.toffee.model.ChannelInfo
import com.google.gson.Gson
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.launch

class MyChannelVideosViewModel @AssistedInject constructor(
    private val reactionDao: ReactionDao,
    private val activitiesRepo: UserActivitiesRepository, 
    private val getMyChannelAssistedFactory: MyChannelVideosService.AssistedFactory, 
    @Assisted private val isOwner: Int, 
    @Assisted private val channelId: Int) : BasePagingViewModel<ChannelInfo>() {
    
    override val repo: BaseListRepository<ChannelInfo> by lazy {
        BaseListRepositoryImpl({BaseNetworkPagingSource(getMyChannelAssistedFactory.create(MyChannelVideosRequestParams("VOD", isOwner, channelId, 0, 0)))})
    }

    @AssistedInject.Factory
    interface AssistedFactory {
        fun create(isOwner: Int, channelId: Int): MyChannelVideosViewModel
    }

    companion object {
        fun provideFactory(
            assistedFactory: AssistedFactory,
            isOwner: Int, channelId: Int
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return assistedFactory.create(isOwner, channelId) as T
            }
        }
    }

    /*fun getReactionByContent(contentId: String): ReactionInfo? {
        var reactionInfo: ReactionInfo? = null
        viewModelScope.launch {
            reactionInfo = async { reactionDao.getReactionByContentId(contentId)}.await()
        }
        return reactionInfo
    }*/

    fun insert(reactionInfo: ReactionInfo) {
        viewModelScope.launch {
            reactionDao.insert(reactionInfo)
        }
    }

    fun insertActivity(channelInfo: ChannelInfo, reactStatus: Int) {
        viewModelScope.launch {
            val item = UserActivities(
                channelInfo.id.toLong(),
                "activity",
                channelInfo.type ?: "VOD",
                Gson().toJson(channelInfo),
                ActivityType.REACT.value,
                reactStatus
            )
            activitiesRepo.insert(item)
        }
    }
}