package com.banglalink.toffee.ui.mychannel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.apiservice.MyChannelPlayListDeleteService
import com.banglalink.toffee.data.network.util.resultFromResponse
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.DeletePlayListBean
import com.banglalink.toffee.model.Resource
import kotlinx.coroutines.launch

class MyChannelPlaylistDeleteViewModel @ViewModelInject constructor(private val apiService: MyChannelPlayListDeleteService): ViewModel() {
    
    private val _data = MutableLiveData<Resource<DeletePlayListBean>>()
    val liveData = _data.toLiveData()
    
    fun deletePlaylistName(playlistId: Int){
        viewModelScope.launch { 
            _data.postValue(resultFromResponse { apiService.invoke(playlistId) })
        }
    }
}