package com.banglalink.toffee.ui.notification

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.common.paging.BaseListRepository
import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.common.paging.BasePagingViewModel
import com.banglalink.toffee.data.database.entities.NotificationInfo
import com.banglalink.toffee.data.repository.NotificationInfoRepository
import kotlinx.coroutines.launch

class NotificationDropdownViewModel @ViewModelInject constructor(private val notificationRepository: NotificationInfoRepository) :
    BasePagingViewModel<NotificationInfo>() {
    override val repo: BaseListRepository<NotificationInfo> by lazy {
        BaseListRepositoryImpl({notificationRepository.getAllNotification()})
    }

    fun setSeenStatus(id: Long, isSeen: Boolean, seenTime: Long) {
        viewModelScope.launch {
            notificationRepository.updateSeenStatus(id, isSeen, seenTime)
        }
    }
    
    fun deleteNotification(notificationInfo: NotificationInfo){
        viewModelScope.launch { 
            notificationRepository.delete(notificationInfo)
        }
    }
}