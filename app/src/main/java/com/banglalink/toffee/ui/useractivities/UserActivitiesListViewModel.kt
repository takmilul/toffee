package com.banglalink.toffee.ui.useractivities

import com.banglalink.toffee.data.network.retrofit.RetrofitApiClient
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.SingleListRepository
import com.banglalink.toffee.ui.common.SingleListViewModel
import com.banglalink.toffee.usecase.UserActivities

class UserActivitiesListViewModel: SingleListViewModel<ChannelInfo>() {
    override var repo: SingleListRepository<ChannelInfo> = UserActivities(Preference.getInstance(), RetrofitApiClient.toffeeApi)
}
