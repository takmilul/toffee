package com.banglalink.toffee.ui.recent

import androidx.hilt.lifecycle.ViewModelInject
import com.banglalink.toffee.apiservice.GetHistory
import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.common.paging.BaseNetworkPagingSource
import com.banglalink.toffee.common.paging.BasePagingViewModel
import com.banglalink.toffee.model.ChannelInfo

class RecentViewModel @ViewModelInject constructor(
    private val apiService: GetHistory
): BasePagingViewModel<ChannelInfo>() {
    override val repo by lazy {
        BaseListRepositoryImpl(
            BaseNetworkPagingSource(apiService)
        )
    }
}