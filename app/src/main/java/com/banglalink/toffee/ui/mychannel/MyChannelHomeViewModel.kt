package com.banglalink.toffee.ui.mychannel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.apiservice.MyChannelGetDetailService
import com.banglalink.toffee.apiservice.MyChannelRatingService
import com.banglalink.toffee.data.network.util.resultFromResponse
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.MyChannelRatingBean
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.model.MyChannelDetailBean
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.launch

class MyChannelHomeViewModel @AssistedInject constructor(private val apiService: MyChannelGetDetailService, private val ratingService: MyChannelRatingService, @Assisted private val isOwner: Int, @Assisted private val channelId: Int) :
    ViewModel() {

    private val _data = MutableLiveData<Resource<MyChannelDetailBean?>>()
    val liveData = _data.toLiveData()
    private val _ratingData = MutableLiveData<Resource<MyChannelRatingBean>>()
    val ratingLiveData = _ratingData.toLiveData()

    init {
        viewModelScope.launch { 
            _data.postValue(resultFromResponse { apiService.execute(isOwner, channelId) })
        }
    }

    @AssistedInject.Factory
    interface AssistedFactory {
        fun create(isOwner: Int, channelId: Int): MyChannelHomeViewModel
    }

    companion object {
        fun provideFactory(
            assistedFactory: AssistedFactory, isOwner: Int, channelId: Int
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                    return assistedFactory.create(isOwner, channelId) as T
                }
            }
    }

    fun rateMyChannel(rating: Float){
        viewModelScope.launch { 
            _ratingData.postValue(resultFromResponse { ratingService.execute(channelId, rating) })
        }
    }
}