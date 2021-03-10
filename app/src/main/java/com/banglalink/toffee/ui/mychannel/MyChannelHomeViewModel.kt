package com.banglalink.toffee.ui.mychannel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.apiservice.MyChannelGetDetailService
import com.banglalink.toffee.apiservice.MyChannelRatingService
import com.banglalink.toffee.data.network.util.resultFromResponse
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.MyChannelDetailBean
import com.banglalink.toffee.model.MyChannelRatingBean
import com.banglalink.toffee.model.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyChannelHomeViewModel @Inject constructor(
    private val apiService: MyChannelGetDetailService,
    private val ratingService: MyChannelRatingService,
) : ViewModel() {
    
    private val _data = MutableLiveData<Resource<MyChannelDetailBean?>>()
    val liveData = _data.toLiveData()
    private val _ratingData = MutableLiveData<Resource<MyChannelRatingBean>>()
    val ratingLiveData = _ratingData.toLiveData()
    
    fun getChannelDetail(channelOwnerId: Int) {
        viewModelScope.launch {
            _data.postValue(resultFromResponse { apiService.execute(channelOwnerId) })
        }
    }
    
    fun rateMyChannel(channelOwnerId: Int, rating: Float) {
        viewModelScope.launch {
            _ratingData.postValue(resultFromResponse { ratingService.execute(channelOwnerId, rating) })
        }
    }
}