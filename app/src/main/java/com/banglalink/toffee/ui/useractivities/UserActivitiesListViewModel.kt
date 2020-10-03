package com.banglalink.toffee.ui.useractivities

import androidx.hilt.lifecycle.ViewModelInject
import com.banglalink.toffee.common.paging.BasePagingViewModel
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.apiservice.UserActivities

class UserActivitiesListViewModel @ViewModelInject constructor(
    override val apiService: UserActivities
): BasePagingViewModel<ChannelInfo>()