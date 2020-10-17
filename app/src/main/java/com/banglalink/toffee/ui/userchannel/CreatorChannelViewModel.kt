package com.banglalink.toffee.ui.userchannel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.data.network.util.resultFromResponse
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.model.UgcMyChannelDetailBean
import com.banglalink.toffee.usecase.GetUgcMyChannelDetail
import com.banglalink.toffee.usecase.MyChannelDetailParam
import com.banglalink.toffee.util.unsafeLazy
import kotlinx.coroutines.launch

class CreatorChannelViewModel(private val myChannelDetailAssistedFactory: GetUgcMyChannelDetail.AssistedFactory): ViewModel() {
    
    private val _data = MutableLiveData<Resource<UgcMyChannelDetailBean?>>()
    var liveData = _data.toLiveData()
    val channelInfo = MutableLiveData<UgcMyChannelDetailBean?>()
    
    private val getChannelInfo by unsafeLazy { myChannelDetailAssistedFactory.create(MyChannelDetailParam()) }
    
    fun loadData() {
        viewModelScope.launch {
            _data.postValue(resultFromResponse { getChannelInfo.execute() })
        }
    }
    
}