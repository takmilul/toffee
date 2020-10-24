package com.banglalink.toffee.ui.mychannel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.apiservice.MyChannelPlaylistCreateService
import com.banglalink.toffee.apiservice.MyChannelPlaylistEditService
import com.banglalink.toffee.data.network.util.resultFromResponse
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.model.MyChannelPlaylistCreateBean
import com.banglalink.toffee.model.MyChannelPlaylistEditBean
import kotlinx.coroutines.launch

class MyChannelPlaylistCreateViewModel @ViewModelInject constructor(private val createPlaylistApiService: MyChannelPlaylistCreateService, private val editPlaylistApiService: MyChannelPlaylistEditService) : ViewModel() {

    var playlistName: String? = null
    private val _createPlaylistData = MutableLiveData<Resource<MyChannelPlaylistCreateBean>>()
    val createPlaylistLiveData = _createPlaylistData.toLiveData()
    private val _editPlaylistData = MutableLiveData<Resource<MyChannelPlaylistEditBean>>()
    val editPlaylistLiveData = _editPlaylistData.toLiveData()

    fun createPlaylist(isOwner: Int, channelId: Int) {
        viewModelScope.launch {
            _createPlaylistData.postValue(resultFromResponse { createPlaylistApiService.execute(isOwner, channelId, playlistName!!) })
        }
    }

    fun editPlaylist(playlistId: Int) {
        viewModelScope.launch {
            _editPlaylistData.postValue(resultFromResponse { editPlaylistApiService.execute(playlistId, playlistName!!) })
        }
    }
}