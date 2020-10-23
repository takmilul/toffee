package com.banglalink.toffee.ui.userchannel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.apiservice.GetChannelVideos
import com.banglalink.toffee.apiservice.MyChannelRequestParams
import com.banglalink.toffee.common.paging.BaseListRepository
import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.common.paging.BaseNetworkPagingSource
import com.banglalink.toffee.common.paging.BasePagingViewModel
import com.banglalink.toffee.data.database.dao.ReactionDao
import com.banglalink.toffee.data.database.entities.ReactionInfo
import com.banglalink.toffee.model.ChannelInfo
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.launch

class ChannelVideosViewModel @AssistedInject constructor(private val reactionDao: ReactionDao, private val getMyChannelAssistedFactory: GetChannelVideos.AssistedFactory, @Assisted private val isOwner: Int, @Assisted private val channelId: Int) : BasePagingViewModel<ChannelInfo>() {
    
    override val repo: BaseListRepository<ChannelInfo> by lazy {
        BaseListRepositoryImpl({BaseNetworkPagingSource(getMyChannelAssistedFactory.create(MyChannelRequestParams("VOD", isOwner, channelId, 0, 0)))})
    }

    @AssistedInject.Factory
    interface AssistedFactory {
        fun create(isOwner: Int, channelId: Int): ChannelVideosViewModel
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
}