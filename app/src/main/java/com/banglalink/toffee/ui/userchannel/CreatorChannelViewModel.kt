package com.banglalink.toffee.ui.userchannel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.data.network.util.resultFromResponse
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.model.Resource.Failure
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.model.UgcMyChannelDetailBean
import com.banglalink.toffee.usecase.GetUgcMyChannelDetail
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.launch

class CreatorChannelViewModel @AssistedInject constructor(private val apiService: GetUgcMyChannelDetail, @Assisted private val isOwner: Int, @Assisted private val channelId: Int) :
    ViewModel() {

    private val _data = MutableLiveData<Resource<UgcMyChannelDetailBean?>>()
    var liveData = _data.toLiveData()
    val channelInfo = MutableLiveData<UgcMyChannelDetailBean>()

    @AssistedInject.Factory
    interface AssistedFactory {
        fun create(isOwner: Int, channelId: Int): CreatorChannelViewModel
    }

    companion object {
        fun provideFactory(
            assistedFactory: AssistedFactory,
            isOwner: Int, channelId: Int
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return assistedFactory.create(isOwner, channelId) as T
            }
        }
    }

    fun loadData() {
        viewModelScope.launch {
            val response = resultFromResponse { apiService.execute(isOwner, channelId) }
            when (response) {
                is Success -> channelInfo.postValue(response.data)
                is Failure -> {
                }
            }
        }
    }
}