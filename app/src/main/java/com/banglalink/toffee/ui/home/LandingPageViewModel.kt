package com.banglalink.toffee.ui.home

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.banglalink.toffee.R
import com.banglalink.toffee.apiservice.*
import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.common.paging.BaseNetworkPagingSource
import com.banglalink.toffee.data.network.request.ChannelRequestParams
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.MyChannelPlaylist
import com.banglalink.toffee.model.UgcCategory
import com.banglalink.toffee.model.UgcUserChannelInfo
import com.banglalink.toffee.ui.common.BaseViewModel
import com.banglalink.toffee.ui.mychannel.MyChannelHomeFragment
import kotlinx.coroutines.flow.Flow

class LandingPageViewModel @ViewModelInject constructor(
    private val mostPopularApi: GetMostPopularContents,
    private val mostPopularPlaylists: GetMostPopularPlaylists,
    private val categoryListApi: GetUgcCategories,
    private val popularChannelAssistedFactory: GetUgcPopularUserChannels.AssistedFactory,
    private val trendingNowAssistedFactory: GetUgcTrendingNowContents.AssistedFactory,
    private val featuredContentAssistedFactory: GetFeatureContents.AssistedFactory,
    private val getContentAssistedFactory: GetContents.AssistedFactory
):BaseViewModel() {
    val latestVideoLiveData = MutableLiveData<Pair<Int, Int>>()

    fun loadChannels(): Flow<PagingData<ChannelInfo>>{
        return channelRepo.getList().cachedIn(viewModelScope)
    }

    fun loadLatestVideos(): Flow<PagingData<ChannelInfo>> {
        return latestVideosRepo.getList().cachedIn(viewModelScope)
    }

    fun loadMostPopularVideos(): Flow<PagingData<ChannelInfo>> {
        return mostPopularRepo.getList().cachedIn(viewModelScope)
    }

    fun loadMostPopularPlaylists(): Flow<PagingData<MyChannelPlaylist>> {
        return mostPopularPlaylistsRepo.getList()
    }

    fun loadFeatureContents(): Flow<PagingData<ChannelInfo>>{
        return featureRepo.getList().cachedIn(viewModelScope)
    }

    fun loadCategories(): Flow<PagingData<UgcCategory>> {
        return categoryListRepo.getList().cachedIn(viewModelScope)
    }

    fun loadTrendingNowContent(): Flow<PagingData<ChannelInfo>> {
        return trendingNowRepo.getList().cachedIn(viewModelScope)
    }

    fun loadUserChannels(): Flow<PagingData<UgcUserChannelInfo>> {
        return userChannelRepo.getList().cachedIn(viewModelScope)
    }

    private val userChannelRepo by lazy {
        BaseListRepositoryImpl({
            BaseNetworkPagingSource(
                popularChannelAssistedFactory.create(
                    ApiCategoryRequestParams("", 0, 0)
                )
            )
        })
    }

    fun loadUserChannelsByCategory(category: UgcCategory): Flow<PagingData<UgcUserChannelInfo>> {
        return BaseListRepositoryImpl({
            BaseNetworkPagingSource(
                popularChannelAssistedFactory.create(
                    ApiCategoryRequestParams("", 1, category.id.toInt())
                )
            )
        }).getList()
    }

    private val channelRepo by lazy {
        BaseListRepositoryImpl({
            BaseNetworkPagingSource(
                getContentAssistedFactory.create(
                    ChannelRequestParams("", 0, "", 0, "LIVE")
                )
            )
        } )
    }

    private val categoryListRepo by lazy {
        BaseListRepositoryImpl({
            BaseNetworkPagingSource(
                categoryListApi
            )
        })
    }

    private val latestVideosRepo by lazy {
        BaseListRepositoryImpl({
            BaseNetworkPagingSource(
                getContentAssistedFactory.create(
                    ChannelRequestParams("", 0, "", 0, "VOD")
                )
            )
        })
    }

    private val featureRepo by lazy {
        BaseListRepositoryImpl({
            BaseNetworkPagingSource(
                featuredContentAssistedFactory.create(
                    ApiCategoryRequestParams("VOD", 0, 0)
                )
            )
        })
    }

    private val mostPopularRepo by lazy {
        BaseListRepositoryImpl({
            BaseNetworkPagingSource(
                mostPopularApi
            )
        })
    }

    private val mostPopularPlaylistsRepo by lazy {
        BaseListRepositoryImpl({
            BaseNetworkPagingSource(
                mostPopularPlaylists
            )
        })
    }

    private val trendingNowRepo by lazy {
        BaseListRepositoryImpl({
            BaseNetworkPagingSource(
                trendingNowAssistedFactory.create(
                    ApiCategoryRequestParams("VOD", 0, 0)
                )
            )
        })
    }

    fun loadLatestVideosByCategory(categoryId: Int, subCategoryId: Int): Flow<PagingData<ChannelInfo>> {
        return BaseListRepositoryImpl({
            BaseNetworkPagingSource(
                getContentAssistedFactory.create(
                    ChannelRequestParams("", categoryId, "", subCategoryId, "VOD")
                )
            )
        }).getList()
    }

    fun loadTrendingNowContentByCategory(category: UgcCategory): Flow<PagingData<ChannelInfo>> {
        return BaseListRepositoryImpl({
            BaseNetworkPagingSource(
                trendingNowAssistedFactory.create(
                    ApiCategoryRequestParams("VOD", 1, category.id.toInt())
                )
            )
        }).getList()
    }

    fun loadSubcategoryVideos(catId: Int, subCatId: Int) {
        latestVideoLiveData.value = Pair(catId, subCatId)
    }
    
    fun navigateToMyChannel(fragment: Fragment, providerId: String, isSubscribed: Boolean){
        val customerId = Preference.getInstance().customerId
        val isOwner = if (providerId.toInt() == customerId) 1 else 0
        val isPublic = if (providerId.toInt() == customerId) 0 else 1
        val channelId = providerId.toInt()
        val subscribed = if(isSubscribed) 1 else 0
        findNavController(fragment).navigate(R.id.myChannelHomeFragment, Bundle().apply {
            putInt(MyChannelHomeFragment.IS_SUBSCRIBED, subscribed)
            Log.i("UGC_Home", "onItemClicked: $subscribed")
            putInt(MyChannelHomeFragment.IS_OWNER, isOwner)
            putInt(MyChannelHomeFragment.CHANNEL_ID, channelId)
            putInt(MyChannelHomeFragment.IS_PUBLIC, isPublic)
            putInt(MyChannelHomeFragment.CHANNEL_OWNER_ID, providerId.toInt())
            putBoolean(MyChannelHomeFragment.IS_FROM_OUTSIDE, true)
        })
        /*if (findNavController(fragment).currentDestination?.id == R.id.menu_feed) {
            findNavController(fragment).navigate(R.id.action_menu_feed_to_myChannelHomeFragment, Bundle().apply {
                putInt(MyChannelHomeFragment.IS_SUBSCRIBED, subscribed)
                Log.i("UGC_Home", "onItemClicked: $subscribed")
                putInt(MyChannelHomeFragment.IS_OWNER, isOwner)
                putInt(MyChannelHomeFragment.CHANNEL_ID, channelId)
                putInt(MyChannelHomeFragment.IS_PUBLIC, isPublic)
                putInt(MyChannelHomeFragment.CHANNEL_OWNER_ID, providerId.toInt())
                putBoolean(MyChannelHomeFragment.IS_FROM_OUTSIDE, true)
            })
        }
        else {
            findNavController(fragment).navigate(R.id.action_categoryDetailsFragment_to_myChannelHomeFragment, Bundle().apply {
                putInt(MyChannelHomeFragment.IS_SUBSCRIBED, subscribed)
                Log.i("UGC_Home", "onItemClicked: $subscribed")
                putInt(MyChannelHomeFragment.IS_OWNER, isOwner)
                putInt(MyChannelHomeFragment.CHANNEL_ID, channelId)
                putInt(MyChannelHomeFragment.IS_PUBLIC, isPublic)
                putInt(MyChannelHomeFragment.CHANNEL_OWNER_ID, providerId.toInt())
                putBoolean(MyChannelHomeFragment.IS_FROM_OUTSIDE, true)
            })
        }*/
    }
}