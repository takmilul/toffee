package com.banglalink.toffee.ui.notification

import androidx.hilt.lifecycle.ViewModelInject
import com.banglalink.toffee.apiservice.GetNotifications
import com.banglalink.toffee.common.paging.BaseListRepository
import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.common.paging.BaseNetworkPagingSource
import com.banglalink.toffee.common.paging.BasePagingViewModel
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.Notification

class NotificationDropdownViewModel @ViewModelInject constructor(apiService: GetNotifications) : BasePagingViewModel<Notification>() {
    override val repo: BaseListRepository<Notification> by lazy {
        BaseListRepositoryImpl(BaseNetworkPagingSource(apiService))
    }
}