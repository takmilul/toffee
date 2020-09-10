package com.banglalink.toffee.ui.userchannel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.data.network.retrofit.RetrofitApiClient
import com.banglalink.toffee.data.network.util.resultFromResponse
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.model.UserChannel
import com.banglalink.toffee.usecase.GetChannelInfo
import com.banglalink.toffee.util.unsafeLazy
import kotlinx.coroutines.launch

class CreatorChannelViewModel: ViewModel() {
    
    private val _data = MutableLiveData<Resource<UserChannel?>>()
    var liveData = _data.toLiveData()
    val channelInfo = MutableLiveData<UserChannel?>()
    
    private val getChannelInfo by unsafeLazy { GetChannelInfo(Preference.getInstance(), RetrofitApiClient.toffeeApi) }
    
    fun loadData() {
        viewModelScope.launch {
            _data.postValue(resultFromResponse { getChannelInfo.execute() })
        }
    }
    
}