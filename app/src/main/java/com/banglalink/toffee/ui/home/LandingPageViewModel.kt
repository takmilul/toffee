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
    private val mostPopularApi: GetMostPopularContents,
    private val categoryListApi: GetUgcCategories,
    private val popularChannelAssistedFactory: GetUgcPopularUserChannels.AssistedFactory,
    private val trendingNowAssistedFactory: GetUgcTrendingNowContents.AssistedFactory,
    private val featuredContentAssistedFactory: GetFeatureContents.AssistedFactory,
    private val getContentAssistedFactory: GetContents.AssistedFactory
):BaseViewModel() {
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

    fun loadUserChannels(): Flow<PagingData<UgcUserChannelInfo>> {
        return userChannelRepo.getList()
    }

    private val userChannelRepo by lazy {
        BaseListRepositoryImpl(
            BaseNetworkPagingSource(
                popularChannelAssistedFactory.create(
                    ApiCategoryRequestParams("", 0, 0)
                )
            )
        )
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
                    ApiCategoryRequestParams("VOD", 0, 0)
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
                    ApiCategoryRequestParams("VOD", 1, 0)
                )
            )
        )
    }
}