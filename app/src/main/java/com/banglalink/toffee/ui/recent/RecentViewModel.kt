package com.banglalink.toffee.ui.recent

import android.app.Application
import androidx.lifecycle.LiveData
import com.banglalink.toffee.data.network.retrofit.RetrofitApiClient
import com.banglalink.toffee.data.network.util.resultLiveData
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseViewModel
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.usecase.GetHistory
import com.banglalink.toffee.util.unsafeLazy

class RecentViewModel(application: Application):BaseViewModel(application) {
    private val getHistory by unsafeLazy {
        GetHistory(Preference.getInstance(),RetrofitApiClient.toffeeApi)
    }

    fun loadRecentItems(): LiveData<Resource<List<ChannelInfo>>> {
        return resultLiveData {
            getHistory.execute()
        }
    }

}