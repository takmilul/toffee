package com.banglalink.toffee.ui.home

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.R
import com.banglalink.toffee.data.network.retrofit.RetrofitApiClient
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.extension.setError
import com.banglalink.toffee.extension.setSuccess
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.*
import com.banglalink.toffee.ui.common.BaseViewModel
import com.banglalink.toffee.usecase.*
import com.banglalink.toffee.util.SingleLiveEvent
import com.banglalink.toffee.util.getError
import com.banglalink.toffee.util.unsafeLazy
import kotlinx.coroutines.launch

class LandingPageViewModel(application: Application):BaseViewModel(application) {

    //LiveData for fetching channel list
    private val channelMutableLiveData = MutableLiveData<Resource<List<ChannelInfo>>>()
    val channelLiveData = channelMutableLiveData.toLiveData()

    //LiveData for fetching popular list
    private val popularVideoMutableLiveData = MutableLiveData<Resource<List<ChannelInfo>>>()
    val popularVideoLiveData = popularVideoMutableLiveData.toLiveData()

    private val mostPopularVideoMutableLiveData = MutableLiveData<Resource<List<ChannelInfo>>>()
    val mostPopularVideoLiveData = mostPopularVideoMutableLiveData.toLiveData()

    //LiveData for featureContent List
    private val featureContentMutableLiveData = MutableLiveData<Resource<List<ChannelInfo>>>()
    val featureContentLiveData = featureContentMutableLiveData.toLiveData()

    private val userChannelListMutableLiveData = MutableLiveData<Resource<List<ChannelInfo>>>()
    val userChannelList = userChannelListMutableLiveData.toLiveData()

//    //LiveData for Categories List
//    private val categoriesMutableLiveData = MutableLiveData<List<Category>>()
//    val categoriesLiveData = categoriesMutableLiveData.toLiveData()

    private val trendingNowMutableLiveData = MutableLiveData<List<ChannelInfo>>()
    val trendingNowLiveData = trendingNowMutableLiveData.toLiveData()

    val categoryInfoLiveData = MutableLiveData<Resource<List<NavCategory>>>()

    private val getCategory by lazy {
        GetCategoryNew(RetrofitApiClient.toffeeApi)
    }

    private val getChannels by unsafeLazy {
        GetContents(RetrofitApiClient.toffeeApi)
    }

    private val getPopularVideo by unsafeLazy {
        GetContents(RetrofitApiClient.toffeeApi)
    }

    private val getFeatureContents by unsafeLazy {
        GetFeatureContents(Preference.getInstance(),RetrofitApiClient.toffeeApi)
    }

    private val getUserChannels by unsafeLazy {
        GetChannelSubscriptions(Preference.getInstance(), RetrofitApiClient.toffeeApi)
    }

    fun loadChannels(){
        viewModelScope.launch {
            try{
                val response = getChannels.execute("",0,"",0,"LIVE")
                channelMutableLiveData.setSuccess(response)
            }catch (e:Exception){
                channelMutableLiveData.setError(getError(e))
            }
        }
    }

    fun loadPopularVideos(){
        viewModelScope.launch {
            try{
                val response = getPopularVideo.execute("",0,"",0,"VOD")
                popularVideoMutableLiveData.setSuccess(response)
            }catch (e:Exception){
                popularVideoMutableLiveData.setError(getError(e))
            }
        }
    }

    fun loadMostPopularVideos(){
        viewModelScope.launch {
            try{
                val response = getPopularVideo.execute("",0,"",0,"VOD")
                mostPopularVideoMutableLiveData.setSuccess(response)
                trendingNowMutableLiveData.value = response.subList(0, 3)
            }catch (e:Exception){
                mostPopularVideoMutableLiveData.setError(getError(e))
            }
        }
    }

    fun loadUserChannels() {
        viewModelScope.launch {
            try {
                val response = getUserChannels.execute()
                userChannelListMutableLiveData.setSuccess(response)
            } catch (e: Exception) {
                userChannelListMutableLiveData.setError(getError(e))
            }
        }
    }

    fun loadFeatureContents(){
        viewModelScope.launch {
            try{
                val response = getFeatureContents.execute("",0,"",0)
                featureContentMutableLiveData.setSuccess(response)
            }catch (e:Exception){
                featureContentMutableLiveData.setError(getError(e))
            }
        }
    }

    fun loadCategories() {
        viewModelScope.launch {
            try {
                categoryInfoLiveData.setSuccess(getCategory.execute())
            } catch (e: Exception) {
                categoryInfoLiveData.setError(getError(e))
            }
        }
    }
}