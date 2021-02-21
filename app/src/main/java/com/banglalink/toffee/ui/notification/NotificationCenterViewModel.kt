package com.banglalink.toffee.ui.notification

import androidx.hilt.lifecycle.ViewModelInject
import com.banglalink.toffee.common.paging.BaseListRepository
import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.common.paging.BasePagingViewModel
import com.banglalink.toffee.data.database.dao.NotificationDao
import com.banglalink.toffee.data.database.entities.NotificationInfo
import com.banglalink.toffee.data.repository.NotificationInfoRepository

class NotificationCenterViewModel @ViewModelInject constructor(notificationRepo: NotificationInfoRepository) : BasePagingViewModel<NotificationInfo>() {
    override val repo: BaseListRepository<NotificationInfo> by lazy {
        BaseListRepositoryImpl({ notificationRepo.getAllNotification() })
    }
}