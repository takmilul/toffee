package com.banglalink.toffee.ui.notification

import androidx.hilt.lifecycle.ViewModelInject
import com.banglalink.toffee.common.paging.BaseListRepository
import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.common.paging.BasePagingViewModel
import com.banglalink.toffee.data.database.dao.NotificationDao
import com.banglalink.toffee.data.database.entities.NotificationInfo

class NotificationCenterViewModel @ViewModelInject constructor(notificationDao: NotificationDao) : BasePagingViewModel<NotificationInfo>() {
    override val repo: BaseListRepository<NotificationInfo> by lazy {
        BaseListRepositoryImpl(notificationDao.getAllNotification())
    }
}