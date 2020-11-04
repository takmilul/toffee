package com.banglalink.toffee.ui.mychannel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.apiservice.MyChannelPlaylistDeleteService
import com.banglalink.toffee.data.network.util.resultFromResponse
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.MyChannelDeletePlaylistBean
import com.banglalink.toffee.model.Resource
import kotlinx.coroutines.launch

class MyChannelPlaylistDeleteViewModel @ViewModelInject constructor(private val apiService: MyChannelPlaylistDeleteService): ViewModel() {
    
    private val _data = MutableLiveData<Resource<MyChannelDeletePlaylistBean>>()
    val liveData = _data.toLiveData()
    
    fun deletePlaylistName(playlistId: Int){
        viewModelScope.launch { 
            _data.postValue(resultFromResponse { apiService.invoke(playlistId) })
        }
    }
}