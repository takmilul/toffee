package com.banglalink.toffee.ui.useractivities

import androidx.hilt.lifecycle.ViewModelInject
import com.banglalink.toffee.common.paging.BasePagingViewModel
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.apiservice.UserActivities
import com.banglalink.toffee.common.paging.BaseListRepository
import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.common.paging.BaseNetworkPagingSource

class UserActivitiesListViewModel @ViewModelInject constructor(
    apiService: UserActivities
): BasePagingViewModel<ChannelInfo>() {
    override val repo: BaseListRepository<ChannelInfo> by lazy {
        BaseListRepositoryImpl(BaseNetworkPagingSource(apiService))
    }
}