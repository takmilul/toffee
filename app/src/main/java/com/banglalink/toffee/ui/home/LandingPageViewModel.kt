package com.banglalink.toffee.ui.home

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.util.Pair
import android.widget.Toast
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
import com.banglalink.toffee.data.network.util.resultFromResponse
import com.banglalink.toffee.data.repository.TVChannelRepository
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.enums.PageType
import com.banglalink.toffee.enums.PageType.Landing
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.*
import com.banglalink.toffee.model.Resource.Failure
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.ui.common.BaseViewModel
import com.banglalink.toffee.ui.mychannel.MyChannelHomeFragment
import com.banglalink.toffee.util.unsafeLazy
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class LandingPageViewModel @ViewModelInject constructor(
    @ApplicationContext private val context: Context,
    private val mostPopularApi: GetMostPopularContents.AssistedFactory,
    private val mostPopularPlaylists: GetMostPopularPlaylists,
    private val categoryListApi: GetUgcCategories,
    private val tvChannelRepo: TVChannelRepository,
    private val popularChannelAssistedFactory: GetUgcPopularUserChannels.AssistedFactory,
    private val editorsChoiceAssistedFactory: GetUgcTrendingNowContents.AssistedFactory,
    private val featuredAssistedFactory: FeatureContentService,
    private val getContentAssistedFactory: GetContents.AssistedFactory,
    private val getContentsAssistedFactory: com.banglalink.toffee.usecase.GetContents.AssistedFactory,
    private val relativeContentsFactory: GetRelativeContents.AssistedFactory
):BaseViewModel() {
    
    val latestVideoLiveData = MutableLiveData<Pair<Int, Int>>()
    val checkedSubCategoryChipId = MutableLiveData<Int>()
    val pageType: MutableLiveData<PageType> = MutableLiveData()
    val categoryId: MutableLiveData<Int> = MutableLiveData()
    val subCategoryId: MutableLiveData<Int> = MutableLiveData()
    val isDramaSeries: MutableLiveData<Boolean> = MutableLiveData()
    private val featuredContentList: MutableLiveData<Resource<List<ChannelInfo>?>> = MutableLiveData()
    val featuredContents = featuredContentList.toLiveData()
    private val subCategoryList: MutableLiveData<Resource<List<UgcSubCategory>>> = MutableLiveData()
    val subCategories = subCategoryList.toLiveData()

    private val hashtagData = MutableLiveData<List<String>>()
    val hashtagList = hashtagData.toLiveData()
    val selectedHashTag = MutableLiveData<String>()

    private val channelMutableLiveData = MutableLiveData<Resource<List<ChannelInfo>>>()
    val channelLiveData = channelMutableLiveData.toLiveData()
    
    val loadChannels by lazy {
        channelRepo.getList().cachedIn(viewModelScope)
    }

    private val getChannels by unsafeLazy {
        getContentsAssistedFactory.create(ChannelRequestParams("",0,"",0,"LIVE"))
    }

    /*val loadLatestVideos by lazy {
        latestVideosRepo.getList().cachedIn(viewModelScope)
    }*/

    /*val loadMostPopularVideos by lazy {
        mostPopularRepo.getList().cachedIn(viewModelScope)
    }*/

    fun loadMostPopularPlaylists(): Flow<PagingData<MyChannelPlaylist>> {
        return mostPopularPlaylistsRepo.getList()
    }

    fun loadFeaturedContentList(){
        viewModelScope.launch {
            val response = resultFromResponse { featuredAssistedFactory.loadData("VOD", pageType.value?:Landing, categoryId.value?:0) }

            when (response) {
                is Success -> {
                    response.data.channels?.let {
                        featuredContentList.postValue(resultFromResponse { it })
                    }
                    response.data.subcategories?.let {
                        if (it.isNotEmpty()) {
                            subCategoryList.postValue(resultFromResponse { it })
                        }
                    }
                    response.data.hashTags?.let {
                        if (it.isNotBlank()) {
                            hashtagData.value = it.split(",").filter { ht -> ht.isNotBlank() }
                        }
                    }
                }
                is Failure -> {
                    Toast.makeText(context, response.error.msg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val loadCategories by lazy {
        categoryListRepo.getList().cachedIn(viewModelScope)
    }

    fun loadEditorsChoiceContent(): Flow<PagingData<ChannelInfo>> {
        return editorsChoiceRepo.getList().cachedIn(viewModelScope)
    }

    val loadUserChannels by lazy {
        userChannelRepo.getList().cachedIn(viewModelScope)
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

    fun loadChannels(){
        viewModelScope.launch {
            val response = resultFromResponse { getChannels.execute() }
            channelMutableLiveData.postValue(response)
        }
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

    fun loadLatestVideos (): Flow<PagingData<ChannelInfo>> {
        return BaseListRepositoryImpl({
            BaseNetworkPagingSource(
                getContentAssistedFactory.create(
                    ChannelRequestParams("", categoryId.value?:0, "", subCategoryId.value?:0, "VOD")
                )
            )
        }).getList()
    }

    fun loadMostPopularVideos (): Flow<PagingData<ChannelInfo>> {
        return BaseListRepositoryImpl({
            BaseNetworkPagingSource(
                mostPopularApi.create(
                    TrendingNowRequestParam("VOD", categoryId.value?:0, subCategoryId.value?:0, isDramaSeries.value?:false)
                )
            )
        }).getList()
    }

    private val mostPopularPlaylistsRepo by lazy {
        BaseListRepositoryImpl({
            BaseNetworkPagingSource(
                mostPopularPlaylists
            )
        })
    }

    private val editorsChoiceRepo by lazy {
        BaseListRepositoryImpl({
            BaseNetworkPagingSource(
                editorsChoiceAssistedFactory.create(
                    EditorsChoiceFeaturedRequestParams("VOD", pageType.value?:Landing, categoryId.value?:0)
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

    fun loadSubcategoryVideos(catId: Int, subCatId: Int) {
        latestVideoLiveData.value = Pair(catId, subCatId)
    }

    val loadPopularMovieChannels by lazy {
        BaseListRepositoryImpl({
            tvChannelRepo.getPopularMovieChannels()
        }).getList()
    }
    
    val loadHashTagContents by lazy { 
        relativeRepo.getList()
    }
    
    private val relativeRepo by lazy {
        BaseListRepositoryImpl({
            BaseNetworkPagingSource(
                relativeContentsFactory.create(
                    CatchupParams("null", selectedHashTag.value)
                )
            )
        })
    }
    
    fun navigateToMyChannel(fragment: Fragment, channelId: Int, channelOwnerId: Int, isSubscribed: Int){
        val customerId = Preference.getInstance().customerId
        val isOwner = if (channelOwnerId == customerId) 1 else 0
        val isPublic = if (channelOwnerId == customerId) 0 else 1
        findNavController(fragment).navigate(R.id.myChannelHomeFragment, Bundle().apply {
            putInt(MyChannelHomeFragment.IS_SUBSCRIBED, isSubscribed)
            Log.i("UGC_Home", "onItemClicked: $isSubscribed")
            putInt(MyChannelHomeFragment.IS_OWNER, isOwner)
            putInt(MyChannelHomeFragment.CHANNEL_ID, channelId)
            putInt(MyChannelHomeFragment.IS_PUBLIC, isPublic)
            putInt(MyChannelHomeFragment.CHANNEL_OWNER_ID, channelOwnerId)
        })
    }
}