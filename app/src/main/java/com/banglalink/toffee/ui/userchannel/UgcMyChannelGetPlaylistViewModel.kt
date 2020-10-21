package com.banglalink.toffee.ui.userchannel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.banglalink.toffee.apiservice.UgcGetMyChannelPlaylist

class UgcMyChannelGetPlaylistViewModel @ViewModelInject constructor(private val apiService: UgcGetMyChannelPlaylist): ViewModel() {

    /*private val _data = MutableLiveData<Resource<List<UgcChannelPlaylistBean>()
    var liveData = _data.toLiveData()
    val channelInfo = MutableLiveData<UgcMyChannelDetailBean>()

    fun loadData(isOwner: Int, channelId: Int) {
        viewModelScope.launch {
            when (val response = resultFromResponse { apiService.execute(isOwner, channelId) }) {
                is Success -> channelInfo.postValue(response.data)
                is Failure -> {
                }
            }
        }
    }*/
}