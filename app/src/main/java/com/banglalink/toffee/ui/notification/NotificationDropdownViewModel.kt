package com.banglalink.toffee.ui.notification

import com.banglalink.toffee.model.Notification
import com.banglalink.toffee.ui.common.SingleListRepository
import com.banglalink.toffee.ui.common.SingleListViewModel
import com.banglalink.toffee.usecase.GetNotifications

class NotificationDropdownViewModel : SingleListViewModel<Notification>() {
    override var repo: SingleListRepository<Notification> = GetNotifications()
}