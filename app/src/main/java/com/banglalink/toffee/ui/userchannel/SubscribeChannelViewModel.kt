package com.banglalink.toffee.ui.userchannel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.apiservice.UgcSubscribeChannel
import com.banglalink.toffee.data.network.util.resultFromResponse
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.model.SubscribeChannelBean
import kotlinx.coroutines.launch

class SubscribeChannelViewModel @ViewModelInject constructor(private val apiService: UgcSubscribeChannel): ViewModel() {
    private val _data = MutableLiveData<Resource<SubscribeChannelBean>>()
    val liveData = _data.toLiveData()
    
    fun subscribe(channelId: Int, subStatus: Int){
        viewModelScope.launch { 
            _data.postValue(resultFromResponse { apiService.invoke(channelId, subStatus) })
        }
    }
}