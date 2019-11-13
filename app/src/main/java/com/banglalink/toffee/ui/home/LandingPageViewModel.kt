package com.banglalink.toffee.ui.home

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.data.network.retrofit.RetrofitApiClient
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.extension.setError
import com.banglalink.toffee.extension.setSuccess
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseViewModel
import com.banglalink.toffee.ui.player.ChannelInfo
import com.banglalink.toffee.usecase.GetContents
import com.banglalink.toffee.usecase.GetFeatureContents
import com.banglalink.toffee.usecase.UpdateFavorite
import com.banglalink.toffee.util.SingleLiveEvent
import com.banglalink.toffee.util.getError
import kotlinx.coroutines.launch

class LandingPageViewModel(application: Application):BaseViewModel(application) {

    //LiveData for fetching channel list
    private val channelMutableLiveData = SingleLiveEvent<Resource<List<ChannelInfo>>>()
    val channelLiveData = channelMutableLiveData.toLiveData()

    //LiveData for fetching popular list
    private val popularVideoMutableLiveData = SingleLiveEvent<Resource<List<ChannelInfo>>>()
    val popularVideoLiveData = popularVideoMutableLiveData.toLiveData()

    //LiveData for featureContent List
    private val featureContentMutableLiveData = MutableLiveData<Resource<List<ChannelInfo>>>()
    val featureContentLiveData = featureContentMutableLiveData.toLiveData()

    private val getChannels by lazy {
        GetContents(RetrofitApiClient.toffeeApi)
    }

    private val getPopularVideo by lazy {
        GetContents(RetrofitApiClient.toffeeApi)
    }

    private val getFeatureContents by lazy {
        GetFeatureContents(RetrofitApiClient.toffeeApi)
    }

    fun loadChannels(offset:Int){
        viewModelScope.launch {
            try{
                val response = getChannels.execute("",0,"",0,"LIVE",offset)
                channelMutableLiveData.setSuccess(response)
            }catch (e:Exception){
                channelMutableLiveData.setError(getError(e))
            }
        }
    }

    fun loadPopularVideos(offset:Int){
        viewModelScope.launch {
            try{
                val response = getPopularVideo.execute("",0,"",0,"VOD",offset)
                popularVideoMutableLiveData.setSuccess(response)
            }catch (e:Exception){
                popularVideoMutableLiveData.setError(getError(e))
            }
        }
    }

    fun loadFeatureContents(offset:Int){
        viewModelScope.launch {
            try{
                val response = getFeatureContents.execute("",0,"",0,"VOD",offset)
                featureContentMutableLiveData.setSuccess(response)
            }catch (e:Exception){
                featureContentMutableLiveData.setError(getError(e))
            }
        }
    }


}