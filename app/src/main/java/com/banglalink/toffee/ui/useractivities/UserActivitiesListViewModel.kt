package com.banglalink.toffee.ui.useractivities

import android.app.Application
import androidx.lifecycle.LiveData
import com.banglalink.toffee.data.network.retrofit.RetrofitApiClient
import com.banglalink.toffee.data.network.util.resultLiveData
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseViewModel
import com.banglalink.toffee.usecase.UserActivities
import com.banglalink.toffee.util.unsafeLazy

class UserActivitiesListViewModel(app: Application): BaseViewModel(app) {
    private val getUserActivities by unsafeLazy {
        UserActivities(Preference.getInstance(), RetrofitApiClient.toffeeApi)
    }

    fun loadUserActivities(): LiveData<Resource<List<ChannelInfo>>> {
        return resultLiveData {
            getUserActivities.execute()
        }
    }
}