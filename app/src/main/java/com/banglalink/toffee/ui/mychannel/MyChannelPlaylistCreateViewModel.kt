package com.banglalink.toffee.ui.mychannel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.apiservice.MyChannelPlaylistCreateService
import com.banglalink.toffee.apiservice.MyChannelPlaylistEditService
import com.banglalink.toffee.data.network.util.resultFromResponse
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.MyChannelPlaylistCreateBean
import com.banglalink.toffee.model.MyChannelPlaylistEditBean
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyChannelPlaylistCreateViewModel @Inject constructor(
    private val createPlaylistApiService: MyChannelPlaylistCreateService,
    private val editPlaylistApiService: MyChannelPlaylistEditService,
) : ViewModel() {
    
    var playlistName: String? = null
    private val _createPlaylistData = SingleLiveEvent<Resource<MyChannelPlaylistCreateBean?>>()
    val createPlaylistLiveData = _createPlaylistData.toLiveData()
    private val _editPlaylistData = SingleLiveEvent<Resource<MyChannelPlaylistEditBean?>>()
    val editPlaylistLiveData = _editPlaylistData.toLiveData()
    
    fun createPlaylist(channelOwnerId: Int,isUserPlaylist:Int = 0) {
        viewModelScope.launch {
            val response = resultFromResponse { createPlaylistApiService.execute(channelOwnerId, playlistName!!.trim(),isUserPlaylist) }
            _createPlaylistData.postValue(response)
        }
    }
    
    fun editPlaylist(playlistId: Int, channelOwnerId: Int, isUserPlaylist: Int = 0) {
        viewModelScope.launch {
            val response = resultFromResponse { editPlaylistApiService.execute(playlistId, playlistName!!.trim(), channelOwnerId, isUserPlaylist) }
            _editPlaylistData.postValue(response)
        }
    }
}