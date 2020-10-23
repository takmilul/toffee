package com.banglalink.toffee.ui.userchannel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.apiservice.DeletePlayList
import com.banglalink.toffee.data.network.util.resultFromResponse
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.DeletePlayListBean
import com.banglalink.toffee.model.Resource
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.launch

class UgcMyChannelDeletePlaylistViewModel @ViewModelInject constructor(private val apiService: DeletePlayList): ViewModel() {
    
    private val _data = MutableLiveData<Resource<DeletePlayListBean>>()
    val liveData = _data.toLiveData()
    
    fun deletePlaylistName(playlistId: Int){
        viewModelScope.launch { 
            _data.postValue(resultFromResponse { apiService.invoke(playlistId) })
        }
    }
}