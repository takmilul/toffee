package com.banglalink.toffee.ui.userchannel

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

class UgcMyChannelDeletePlaylistViewModel @AssistedInject constructor(private val apiService: DeletePlayList, @Assisted private val playlistId: Int): ViewModel() {
    
    private val _data = MutableLiveData<Resource<DeletePlayListBean>>()
    val liveData = _data.toLiveData()
    
    @AssistedInject.Factory
    interface AssistedFactory{
        fun create(playlistId: Int): UgcMyChannelDeletePlaylistViewModel
    }
    
    companion object{
        fun provideFactory(assistedFactory: AssistedFactory, playlistId: Int): ViewModelProvider.Factory = 
            object : ViewModelProvider.Factory{
                override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                    return assistedFactory.create(playlistId) as T
                }
            }
    }
    
    fun deletePlaylistName(){
        viewModelScope.launch { 
            _data.postValue(resultFromResponse { apiService.invoke(playlistId) })
        }
    }
}