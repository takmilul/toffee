package com.banglalink.toffee.ui.userchannel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.apiservice.GetUgcCategories
import com.banglalink.toffee.data.network.request.UgcEditMyChannelRequest
import com.banglalink.toffee.data.network.util.resultFromResponse
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.model.UgcCategory
import com.banglalink.toffee.model.UgcEditMyChannelBean
import com.banglalink.toffee.model.UgcMyChannelDetail
import com.banglalink.toffee.ui.common.BaseViewModel
import com.banglalink.toffee.usecase.EditMyChannel
import kotlinx.coroutines.launch

class CreatorChannelEditViewModel @ViewModelInject constructor(private val apiService: EditMyChannel, private val categoryApiService: GetUgcCategories): BaseViewModel() {
    
    val userChannel = MutableLiveData<UgcMyChannelDetail>()
    private val _data = MutableLiveData<Resource<UgcEditMyChannelBean>>()
    val liveData = _data.toLiveData()
    private var _categories = MutableLiveData<List<UgcCategory>>()
    val categories = _categories.toLiveData()

    init {
        viewModelScope.launch {
            _categories.postValue(categoryApiService.loadData(0,0))
        }
    }
    
    fun editChannel(ugcEditMyChannelRequest: UgcEditMyChannelRequest){
        viewModelScope.launch { 
            _data.postValue(resultFromResponse { apiService.execute(ugcEditMyChannelRequest) })
        }
    }
}