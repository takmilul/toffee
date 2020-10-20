package com.banglalink.toffee.ui.userchannel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.data.network.util.resultFromResponse
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.model.UgcCreatePlaylistBean
import com.banglalink.toffee.usecase.UgcCreatePlaylist
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.launch

class UgcMyChannelCreatePlaylistViewModel @ViewModelInject constructor(private val apiService: UgcCreatePlaylist) : ViewModel() {

    var playlistName: String? = null
    private val _data = MutableLiveData<Resource<UgcCreatePlaylistBean>>()
    val liveData = _data.toLiveData()

    fun createPlaylist(isOwner: Int, channelId: Int) {
        viewModelScope.launch {
            _data.postValue(resultFromResponse { apiService.execute(isOwner, channelId, playlistName!!) })
        }
    }
}