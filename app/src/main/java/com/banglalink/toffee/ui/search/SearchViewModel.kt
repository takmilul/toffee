package com.banglalink.toffee.ui.search

import android.app.Application
import androidx.lifecycle.LiveData
import com.banglalink.toffee.data.network.retrofit.RetrofitApiClient
import com.banglalink.toffee.data.network.util.resultLiveData
import com.banglalink.toffee.data.storage.AppDatabase
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseViewModel
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.usecase.GetViewCount
import com.banglalink.toffee.usecase.SearchContent
import com.banglalink.toffee.util.unsafeLazy

class SearchViewModel(application: Application):BaseViewModel(application) {

    private val searchContent by unsafeLazy {
        SearchContent(Preference.getInstance(),RetrofitApiClient.toffeeApi, GetViewCount(AppDatabase.getDatabase().viewCountDAO()))
    }

    fun searchContent(searchKey:String):LiveData<Resource<List<ChannelInfo>>>{
        return resultLiveData {
            searchContent.execute(searchKey)
        }
    }
}
