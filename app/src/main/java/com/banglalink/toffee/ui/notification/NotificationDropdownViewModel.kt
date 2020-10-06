package com.banglalink.toffee.ui.notification

import androidx.hilt.lifecycle.ViewModelInject
import com.banglalink.toffee.apiservice.GetNotifications
import com.banglalink.toffee.common.paging.BasePagingViewModel
import com.banglalink.toffee.model.Notification

class NotificationDropdownViewModel @ViewModelInject constructor(override val apiService: GetNotifications) : BasePagingViewModel<Notification>()