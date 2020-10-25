package com.banglalink.toffee.ui.mychannel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.apiservice.MyChannelPlaylistCreateService
import com.banglalink.toffee.apiservice.MyChannelPlaylistEditService
import com.banglalink.toffee.data.network.util.resultFromResponse
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.MyChannelPlaylistCreateBean
import com.banglalink.toffee.model.MyChannelPlaylistEditBean
import com.banglalink.toffee.model.Resource
import kotlinx.coroutines.launch

class MyChannelPlaylistCreateViewModel @ViewModelInject constructor(private val createPlaylistApiService: MyChannelPlaylistCreateService, private val editPlaylistApiService: MyChannelPlaylistEditService) : ViewModel() {

    var playlistName: String? = null
    private val _createPlaylistData = MutableLiveData<Resource<MyChannelPlaylistCreateBean>>()
    val createPlaylistLiveData = _createPlaylistData.toLiveData()
    private val _editPlaylistData = MutableLiveData<Resource<MyChannelPlaylistEditBean>>()
    val editPlaylistLiveData = _editPlaylistData.toLiveData()

    fun createPlaylist(isOwner: Int, channelId: Int) {
        viewModelScope.launch {
            val newChannelId = if (isOwner == 0) 0 else channelId
            _createPlaylistData.postValue(resultFromResponse { createPlaylistApiService.execute(isOwner, newChannelId, playlistName!!) })
        }
    }

    fun editPlaylist(playlistId: Int, channelId: Int, isOwner: Int) {
        viewModelScope.launch {
            val newChannelId = if (isOwner == 0) 0 else channelId
            _editPlaylistData.postValue(resultFromResponse { editPlaylistApiService.execute(playlistId, playlistName!!, newChannelId, isOwner) })
        }
    }
}