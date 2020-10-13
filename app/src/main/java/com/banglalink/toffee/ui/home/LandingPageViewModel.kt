package com.banglalink.toffee.ui.home

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.banglalink.toffee.apiservice.GetChannelSubscriptions
import com.banglalink.toffee.apiservice.GetMostPopularContents
import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.common.paging.BaseNetworkPagingSource
import com.banglalink.toffee.data.network.request.ChannelRequestParams
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.extension.setError
import com.banglalink.toffee.extension.setSuccess
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.*
import com.banglalink.toffee.ui.common.BaseViewModel
import com.banglalink.toffee.usecase.*
import com.banglalink.toffee.util.getError
import com.banglalink.toffee.util.unsafeLazy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class LandingPageViewModel @ViewModelInject constructor(
    private val mPref: Preference,
    private val toffeeApi: ToffeeApi,
    private val mostPopularApi: GetMostPopularContents,
    private val featuredContentAssistedFactory: com.banglalink.toffee.apiservice.GetFeatureContents.AssistedFactory,
    private val getContentAssistedFactory: com.banglalink.toffee.apiservice.GetContents.AssistedFactory
):BaseViewModel() {

    private val userChannelListMutableLiveData = MutableLiveData<Resource<List<ChannelInfo>>>()
    val userChannelList = userChannelListMutableLiveData.toLiveData()

//    //LiveData for Categories List
//    private val categoriesMutableLiveData = MutableLiveData<List<Category>>()
//    val categoriesLiveData = categoriesMutableLiveData.toLiveData()

    private val trendingNowMutableLiveData = MutableLiveData<List<ChannelInfo>>()
    val trendingNowLiveData = trendingNowMutableLiveData.toLiveData()

    val categoryInfoLiveData = MutableLiveData<Resource<List<NavCategory>>>()

    private val getCategory by lazy {
        GetCategoryNew(toffeeApi)
    }

    private val getUserChannels by unsafeLazy {
        GetChannelSubscriptions(mPref, toffeeApi)
    }

    fun loadChannels(): Flow<PagingData<ChannelInfo>>{
        return channelRepo.getList()
    }

    fun loadLatestVideos(): Flow<PagingData<ChannelInfo>> {
        return latestVideosRepo.getList()
    }

    fun loadMostPopularVideos(): Flow<PagingData<ChannelInfo>> {
        return mostPopularRepo.getList()
    }

    fun loadUserChannels() {
        viewModelScope.launch {
            try {
                val response = getUserChannels.loadData(0, 10)
                userChannelListMutableLiveData.setSuccess(response)
            } catch (e: Exception) {
                userChannelListMutableLiveData.setError(getError(e))
            }
        }
    }

    private val channelRepo by lazy {
        BaseListRepositoryImpl(
            BaseNetworkPagingSource(
                getContentAssistedFactory.create(
                    ChannelRequestParams("", 0, "", 0, "LIVE")
                )
            )
        )
    }

    private val latestVideosRepo by lazy {
        BaseListRepositoryImpl(
            BaseNetworkPagingSource(
                getContentAssistedFactory.create(
                    ChannelRequestParams("", 0, "", 0, "VOD")
                )
            )
        )
    }

    private val featureRepo by lazy {
        BaseListRepositoryImpl(
            BaseNetworkPagingSource(
                featuredContentAssistedFactory.create(
                    ChannelRequestParams("", 0, "", 0, "VOD")
                )
            )
        )
    }

    private val mostPopularRepo by lazy {
        BaseListRepositoryImpl(
            BaseNetworkPagingSource(
//                mostPopularApi
                getContentAssistedFactory.create(
                    ChannelRequestParams("", 0, "", 0, "VOD")
                )
            )
        )
    }

    fun loadFeatureContents(): Flow<PagingData<ChannelInfo>>{
        return featureRepo.getList()
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