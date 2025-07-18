package com.banglalink.toffee.ui.mychannel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.apiservice.MyChannelPlaylistDeleteService
import com.banglalink.toffee.data.network.util.resultFromResponse
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.MyChannelDeletePlaylistBean
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyChannelPlaylistDeleteViewModel @Inject constructor(
    private val apiService: MyChannelPlaylistDeleteService,
) : ViewModel() {
    
    private val _data = SingleLiveEvent<Resource<MyChannelDeletePlaylistBean?>>()
    val liveData = _data.toLiveData()
    
    fun deletePlaylistName(playlistId: Int, isUserPlaylist: Int = 0) {
        viewModelScope.launch {
            val response = resultFromResponse { apiService.invoke(playlistId, isUserPlaylist) }
            _data.postValue(response)
        }
    }
}