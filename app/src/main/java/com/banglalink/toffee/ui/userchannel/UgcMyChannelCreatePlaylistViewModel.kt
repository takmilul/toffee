package com.banglalink.toffee.ui.userchannel

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

class UgcMyChannelCreatePlaylistViewModel @AssistedInject constructor(private val apiService: UgcCreatePlaylist, @Assisted private val isOwner: Int, @Assisted private val channelId: Int) :
    ViewModel() {

    var playlistName: String? = null
    private val _data = MutableLiveData<Resource<UgcCreatePlaylistBean>>()
    val liveData = _data.toLiveData()

    @AssistedInject.Factory
    interface AssistedFactory {
        fun create(isOwner: Int, channelId: Int): UgcMyChannelCreatePlaylistViewModel
    }

    companion object {
        fun provideFactory(assistedFactory: AssistedFactory, isOwner: Int, channelId: Int): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                    return assistedFactory.create(isOwner, channelId) as T
                }
            }
    }

    fun createPlaylist() {
        viewModelScope.launch {
            _data.postValue(resultFromResponse { apiService.execute(isOwner, channelId, playlistName!!) })
        }
    }
}