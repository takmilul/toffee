package com.banglalink.toffee.ui.userchannel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.apiservice.GetChannelVideos
import com.banglalink.toffee.common.paging.BaseListRepository
import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.common.paging.BaseNetworkPagingSource
import com.banglalink.toffee.common.paging.BasePagingViewModel
import com.banglalink.toffee.data.database.dao.ReactionDao
import com.banglalink.toffee.data.database.entities.ReactionInfo
import com.banglalink.toffee.model.ChannelInfo
import kotlinx.coroutines.launch

class ChannelVideosViewModel @ViewModelInject constructor(private val reactionDao: ReactionDao, apiService: GetChannelVideos) : BasePagingViewModel<ChannelInfo>() {
    override val repo: BaseListRepository<ChannelInfo> by lazy {
        BaseListRepositoryImpl(BaseNetworkPagingSource(apiService))
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