package com.banglalink.toffee.ui.userchannel

import androidx.hilt.lifecycle.ViewModelInject
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

class CreatorChannelViewModel @ViewModelInject constructor(private val apiService: GetUgcMyChannelDetail) :
    ViewModel() {

    private val _data = MutableLiveData<Resource<UgcMyChannelDetailBean?>>()
    var liveData = _data.toLiveData()
    val channelInfo = MutableLiveData<UgcMyChannelDetailBean>()

    fun loadData(isOwner: Int, channelId: Int) {
        viewModelScope.launch {
            when (val response = resultFromResponse { apiService.execute(isOwner, channelId) }) {
                is Success -> channelInfo.postValue(response.data)
                is Failure -> {
                }
            }
        }
    }
}