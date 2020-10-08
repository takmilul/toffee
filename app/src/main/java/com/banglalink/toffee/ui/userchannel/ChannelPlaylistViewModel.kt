package com.banglalink.toffee.ui.userchannel

import androidx.hilt.lifecycle.ViewModelInject
import com.banglalink.toffee.apiservice.GetChannelPlaylists
import com.banglalink.toffee.common.paging.BaseListRepository
import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.common.paging.BaseNetworkPagingSource
import com.banglalink.toffee.common.paging.BasePagingViewModel
import com.banglalink.toffee.model.ChannelInfo

class ChannelPlaylistViewModel @ViewModelInject constructor(apiService: GetChannelPlaylists): BasePagingViewModel<ChannelInfo>() {
    override val repo: BaseListRepository<ChannelInfo> by lazy {
        BaseListRepositoryImpl(BaseNetworkPagingSource(apiService))
    }
}