package com.banglalink.toffee.ui.userchannel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.apiservice.UgcCreatePlaylist
import com.banglalink.toffee.apiservice.UgcEditMyChannelPlaylist
import com.banglalink.toffee.data.network.util.resultFromResponse
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.model.UgcCreatePlaylistBean
import com.banglalink.toffee.model.UgcEditPlaylistBean
import kotlinx.coroutines.launch

class UgcMyChannelCreatePlaylistViewModel @ViewModelInject constructor(private val createPlaylistApiService: UgcCreatePlaylist, private val editPlaylistApiService: UgcEditMyChannelPlaylist) : ViewModel() {

    var playlistName: String? = null
    private val _createPlaylistData = MutableLiveData<Resource<UgcCreatePlaylistBean>>()
    val createPlaylistLiveData = _createPlaylistData.toLiveData()
    private val _editPlaylistData = MutableLiveData<Resource<UgcEditPlaylistBean>>()
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