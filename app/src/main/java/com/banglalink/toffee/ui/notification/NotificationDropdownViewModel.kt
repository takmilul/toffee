package com.banglalink.toffee.ui.notification

import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.common.paging.BaseListRepository
import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.common.paging.BasePagingViewModel
import com.banglalink.toffee.data.database.entities.NotificationInfo
import com.banglalink.toffee.data.repository.NotificationInfoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationDropdownViewModel @Inject constructor(
    private val notificationRepository: NotificationInfoRepository,
) : BasePagingViewModel<NotificationInfo>() {
    
    override fun repo(): BaseListRepository<NotificationInfo> {
        return BaseListRepositoryImpl({ notificationRepository.getAllNotification() })
    }
    
    fun setSeenStatus(id: Long, isSeen: Boolean, seenTime: Long) {
        viewModelScope.launch {
            notificationRepository.updateSeenStatus(id, isSeen, seenTime)
        }
    }
    
    fun deleteNotification(notificationInfo: NotificationInfo) {
        viewModelScope.launch {
            notificationRepository.delete(notificationInfo)
        }
    }
}