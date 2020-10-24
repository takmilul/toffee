package com.banglalink.toffee.ui.mychannel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.apiservice.MyChannelAddToPlayListService
import com.banglalink.toffee.data.network.util.resultFromResponse
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.MyChannelAddToPlaylistBean
import com.banglalink.toffee.model.Resource
import kotlinx.coroutines.launch

class MyChannelAddToPlaylistViewModel @ViewModelInject constructor(private val apiService: MyChannelAddToPlayListService): ViewModel() {
    
    private val _data = MutableLiveData<Resource<MyChannelAddToPlaylistBean>>()
    val liveData = _data.toLiveData()
    
    fun addToPlaylist(playlistId: Int, contentId: Int){
        viewModelScope.launch { 
            _data.postValue(resultFromResponse { apiService.invoke(playlistId, contentId) })
        }
    }
}