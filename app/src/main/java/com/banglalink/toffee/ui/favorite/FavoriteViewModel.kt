package com.banglalink.toffee.ui.favorite

import androidx.hilt.lifecycle.ViewModelInject
import com.banglalink.toffee.apiservice.GetFavoriteContents
import com.banglalink.toffee.common.paging.BaseListRepository
import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.common.paging.BaseNetworkPagingSource
import com.banglalink.toffee.common.paging.BasePagingViewModel
import com.banglalink.toffee.model.ChannelInfo

class FavoriteViewModel @ViewModelInject constructor(
    apiService: GetFavoriteContents
): BasePagingViewModel<ChannelInfo>() {

    override val repo: BaseListRepository<ChannelInfo> by lazy {
        BaseListRepositoryImpl(
            BaseNetworkPagingSource(apiService)
        )
    }
}