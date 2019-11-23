package com.banglalink.toffee.ui.recent

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.data.network.retrofit.RetrofitApiClient
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.extension.setError
import com.banglalink.toffee.extension.setSuccess
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseViewModel
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.usecase.GetHistory
import com.banglalink.toffee.util.getError
import kotlinx.coroutines.launch

class RecentViewModel(application: Application):BaseViewModel(application) {
    private val recentMutableLiveData = MutableLiveData<Resource<List<ChannelInfo>>>()
    val recentLiveData = recentMutableLiveData.toLiveData()

    private val getHistory by lazy {
        GetHistory(Preference.getInstance(),RetrofitApiClient.toffeeApi)
    }

    fun loadRecentItems(){
        viewModelScope.launch {
            try{
                val response = getHistory.execute()
                recentMutableLiveData.setSuccess(response)
            }catch (e:Exception){
                recentMutableLiveData.setError(getError(e))
            }
        }
    }

}