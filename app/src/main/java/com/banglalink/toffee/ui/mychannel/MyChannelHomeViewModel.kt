package com.banglalink.toffee.ui.mychannel

import androidx.core.os.bundleOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.analytics.FirebaseParams
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.analytics.ToffeeEvents
import com.banglalink.toffee.apiservice.ApiNames
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
    
    var myRating: Int = 0
    var channelId: Int = 0
    var rating: Float = 0.0f
    var isSubscribed: Int = 0
    var channelOwnerId: Int = 0
    var subscriberCount: Long = 0
    private val _data = MutableLiveData<Resource<MyChannelDetailBean?>>()
    val liveData = _data.toLiveData()
    private val _ratingData = MutableLiveData<Resource<MyChannelRatingBean?>>()
    val ratingLiveData = _ratingData.toLiveData()
    
    fun getChannelDetail(channelOwnerId: Int) {
        viewModelScope.launch {
            val response = resultFromResponse { apiService.execute(channelOwnerId) }
            _data.postValue(response)
        }
    }
    
    fun rateMyChannel(channelOwnerId: Int, rating: Float) {
        viewModelScope.launch {
            val response = resultFromResponse { ratingService.execute(channelOwnerId, rating) }
           if(response is Resource.Success){
               _ratingData.postValue(response)
           } else{
               val error = response as Resource.Failure
               ToffeeAnalytics.logEvent(
                   ToffeeEvents.EXCEPTION,
                   bundleOf(
                       "api_name" to ApiNames.RATE_CHANNEL,
                       FirebaseParams.BROWSER_SCREEN to "User Channel Details",
                       "error_code" to error.error.code,
                       "error_description" to error.error.msg)
               )
           }
        }
    }
}