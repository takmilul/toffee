package com.banglalink.toffee.ui.notification

import com.banglalink.toffee.common.paging.BaseListRepository
import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.common.paging.BasePagingViewModel
import com.banglalink.toffee.data.database.entities.NotificationInfo
import com.banglalink.toffee.data.repository.NotificationInfoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NotificationCenterViewModel @Inject constructor(
    notificationRepo: NotificationInfoRepository,
) : BasePagingViewModel<NotificationInfo>() {

    override val repo: BaseListRepository<NotificationInfo> by lazy {
        BaseListRepositoryImpl({ notificationRepo.getAllNotification() })
    }
}