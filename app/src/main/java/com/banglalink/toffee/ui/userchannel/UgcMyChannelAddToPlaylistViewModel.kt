package com.banglalink.toffee.ui.userchannel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.apiservice.AddToPlayList
import com.banglalink.toffee.data.network.util.resultFromResponse
import com.banglalink.toffee.di.AppCoroutineScope
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.AddToPlayListBean
import com.banglalink.toffee.model.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class UgcMyChannelAddToPlaylistViewModel @ViewModelInject constructor(private val apiService: AddToPlayList): ViewModel() {
    
    private val _data = MutableLiveData<Resource<AddToPlayListBean>>()
    val liveData = _data.toLiveData()
    
    fun addToPlaylist(playlistId: Int, contentId: Int){
        viewModelScope.launch { 
            _data.postValue(resultFromResponse { apiService.invoke(playlistId, contentId) })
        }
    }
}