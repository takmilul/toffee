package com.banglalink.toffee.ui.mychannel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.apiservice.MyChannelSubscribeService
import com.banglalink.toffee.data.network.util.resultFromResponse
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.MyChannelSubscribeBean
import com.banglalink.toffee.model.Resource
import kotlinx.coroutines.launch

class MyChannelSubscribeViewModel @ViewModelInject constructor(private val apiService: MyChannelSubscribeService): ViewModel() {
    private val _data = MutableLiveData<Resource<MyChannelSubscribeBean>>()
    val liveData = _data.toLiveData()
    
    fun subscribe(channelId: Int, subStatus: Int, channelOwnerId: Int){
        viewModelScope.launch { 
            _data.postValue(resultFromResponse { apiService.invoke(channelId, subStatus, channelOwnerId) })
        }
    }
}