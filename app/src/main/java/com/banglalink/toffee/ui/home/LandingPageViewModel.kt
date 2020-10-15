package com.banglalink.toffee.ui.home

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.banglalink.toffee.apiservice.*
import com.banglalink.toffee.apiservice.GetContents
import com.banglalink.toffee.apiservice.GetFeatureContents
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
    private val categoryListApi: GetUgcCategories,
    private val trendingNowAssistedFactory: GetUgcTrendingNowContents.AssistedFactory,
    private val featuredContentAssistedFactory: GetFeatureContents.AssistedFactory,
    private val getContentAssistedFactory: GetContents.AssistedFactory
):BaseViewModel() {

    private val userChannelListMutableLiveData = MutableLiveData<Resource<List<ChannelInfo>>>()
    val userChannelList = userChannelListMutableLiveData.toLiveData()

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

    fun loadFeatureContents(): Flow<PagingData<ChannelInfo>>{
        return featureRepo.getList()
    }

    fun loadCategories(): Flow<PagingData<UgcCategory>> {
        return categoryListRepo.getList()
    }

    fun loadTrendingNowContent(): Flow<PagingData<ChannelInfo>> {
        return trendingNowRepo.getList()
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

    private val categoryListRepo by lazy {
        BaseListRepositoryImpl(
            BaseNetworkPagingSource(
                categoryListApi
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
                    TrendingNowRequestParams("VOD", 0, 0)
                )
            )
        )
    }

    private val mostPopularRepo by lazy {
        BaseListRepositoryImpl(
            BaseNetworkPagingSource(
                mostPopularApi
            )
        )
    }

    private val trendingNowRepo by lazy {
        BaseListRepositoryImpl(
            BaseNetworkPagingSource(
                trendingNowAssistedFactory.create(
                    TrendingNowRequestParams("VOD", 1, 0)
                )
            )
        )
    }
}