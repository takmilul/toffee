package com.banglalink.toffee.ui.home

import android.content.Context
import android.util.Pair
import android.widget.Toast
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.banglalink.toffee.apiservice.*
import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.common.paging.BaseNetworkPagingSource
import com.banglalink.toffee.data.network.request.ChannelRequestParams
import com.banglalink.toffee.data.network.util.resultFromResponse
import com.banglalink.toffee.data.repository.TVChannelRepository
import com.banglalink.toffee.enums.PageType
import com.banglalink.toffee.enums.PageType.Landing
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.*
import com.banglalink.toffee.model.Resource.Failure
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.ui.common.BaseViewModel
import com.banglalink.toffee.util.SingleLiveEvent
import com.banglalink.toffee.util.unsafeLazy
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LandingPageViewModel @Inject constructor(
    private val categoryListApi: GetCategories,
    private val tvChannelRepo: TVChannelRepository,
    @ApplicationContext private val context: Context,
    private val mostPopularPlaylists: GetMostPopularPlaylists,
    private val featuredAssistedFactory: FeatureContentService,
    private val getContentAssistedFactory: GetContents.AssistedFactory,
    private val mostPopularApi: GetMostPopularContents.AssistedFactory,
    private val relativeContentsFactory: GetRelativeContents.AssistedFactory,
    private val popularChannelAssistedFactory: GetPopularUserChannels.AssistedFactory,
    private val editorsChoiceAssistedFactory: GetUgcTrendingNowContents.AssistedFactory,
) : BaseViewModel() {
    
    val latestVideoLiveData = MutableLiveData<Pair<Int, Int>>()
    val checkedSubCategoryChipId = MutableLiveData<Int>()
    val pageType = MutableLiveData<PageType>()
    val categoryId = MutableLiveData<Int>()
    val subCategoryId = MutableLiveData<Int>()
    val isDramaSeries = MutableLiveData<Boolean>()
    private val featuredContentList = SingleLiveEvent<Resource<List<ChannelInfo>?>>()
    val featuredContents = featuredContentList.toLiveData()
    private val subCategoryList = SingleLiveEvent<Resource<List<SubCategory>>>()
    val subCategories = subCategoryList.toLiveData()

    private val hashtagData = SingleLiveEvent<List<String>>()
    val hashtagList = hashtagData.toLiveData()
    val selectedHashTag = MutableLiveData<String>()

    private val channelMutableLiveData = MutableLiveData<Resource<List<ChannelInfo>>>()
    val channelLiveData = channelMutableLiveData.toLiveData()

    val loadChannels by lazy {
        channelRepo.getList().cachedIn(viewModelScope)
    }

    private val getChannels by unsafeLazy {
        getContentAssistedFactory.create(ChannelRequestParams("", 0, "", 0, "LIVE"))
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

    fun loadFeaturedContentList() {
        viewModelScope.launch {
            val response = resultFromResponse { featuredAssistedFactory.loadData("VOD", pageType.value ?: Landing, categoryId.value ?: 0) }

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
                        if (!it.isNullOrEmpty()) {
                            hashtagData.postValue(it)
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

    fun loadUserChannelsByCategory(category: Category): Flow<PagingData<UserChannelInfo>> {
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
        })
    }

    private val categoryListRepo by lazy {
        BaseListRepositoryImpl({
            BaseNetworkPagingSource(
                categoryListApi
            )
        })
    }

    fun loadLatestVideos(): Flow<PagingData<ChannelInfo>> {
        return BaseListRepositoryImpl({
            BaseNetworkPagingSource(
                getContentAssistedFactory.create(
                    ChannelRequestParams("", 0, "", 0, "VOD")
                )
            )
        }).getList().cachedIn(viewModelScope)
    }

    fun loadMostPopularVideos(categoryId: Int, subCategoryId: Int): Flow<PagingData<ChannelInfo>> {
        return BaseListRepositoryImpl({
            BaseNetworkPagingSource(
                mostPopularApi.create(
                    LandingUserChannelsRequestParam("VOD", categoryId, subCategoryId, isDramaSeries.value ?: false)
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
                    EditorsChoiceFeaturedRequestParams("VOD", pageType.value ?: Landing, categoryId.value ?: 0)
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
}