package com.banglalink.toffee.ui.home

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.banglalink.toffee.apiservice.*
import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.common.paging.BaseNetworkPagingSource
import com.banglalink.toffee.data.exception.JobCanceledError
import com.banglalink.toffee.data.network.request.ChannelRequestParams
import com.banglalink.toffee.data.network.util.resultFromResponse
import com.banglalink.toffee.data.repository.TVChannelRepository
import com.banglalink.toffee.enums.PageType
import com.banglalink.toffee.enums.PageType.Landing
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.*
import com.banglalink.toffee.model.Resource.Failure
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LandingPageViewModel @Inject constructor(
    private val categoryListApi: GetCategories,
    private val tvChannelRepo: TVChannelRepository,
    @ApplicationContext private val context: Context,
    private val featuredAssistedFactory: FeatureContentService,
    private val getContentAssistedFactory: GetContents.AssistedFactory,
    private val mostPopularApi: GetMostPopularContents.AssistedFactory,
    private val relativeContentsFactory: GetRelativeContents.AssistedFactory,
    private val popularChannelAssistedFactory: GetPopularUserChannels.AssistedFactory,
    private val featuredPartnerAssistedFactory: FeaturedPartnerService.AssistedFactory,
    private val editorsChoiceAssistedFactory: GetEditorsChoiceContents.AssistedFactory,
) : ViewModel() {
    var featuredJob: Job? = null
    val categoryId = SingleLiveEvent<Int>()
    val pageName = MutableLiveData<String>()
    val subCategoryId = SingleLiveEvent<Int>()
    val pageType = MutableLiveData<PageType>()
    val isDramaSeries = MutableLiveData<Boolean>()
    val moviesChannelCount = SingleLiveEvent<Int>()
    val selectedHashTag = SingleLiveEvent<String>()
    val hashtagList = SingleLiveEvent<List<String>>()
    val checkedSubCategoryChipId = MutableLiveData<Int>()
    val subCategories = SingleLiveEvent<List<SubCategory>>()
    val featuredContents = SingleLiveEvent<List<ChannelInfo>>()

    fun loadChannels(): Flow<PagingData<ChannelInfo>> {
        return channelRepo.getList().cachedIn(viewModelScope)
    }

    fun loadFeaturedContentList() {
        featuredJob = viewModelScope.launch {
            val response = resultFromResponse { featuredAssistedFactory.loadData("VOD", pageType.value ?: Landing, categoryId.value ?: 0) }
            when (response) {
                is Success -> {
                    featuredContents.postValue(response.data.channels ?: emptyList())
                    if (response.data.pageType != Landing) {
                        subCategories.postValue(response.data.subcategories ?: emptyList())
                        hashtagList.postValue(response.data.hashTags ?: emptyList())
                    }
                }
                is Failure -> {
                    if (response.error !is JobCanceledError) {
                        featuredContents.postValue(emptyList())
                        if (pageType.value != Landing) {
                            subCategories.postValue(emptyList())
                            hashtagList.postValue(emptyList())
                        }
                        context.showToast(response.error.msg)
                    }
                }
            }
        }
    }

    fun loadCategories(): Flow<PagingData<Category>> {
        return categoryListRepo.getList().cachedIn(viewModelScope)
    }

    fun loadLandingEditorsChoiceContent(): Flow<PagingData<ChannelInfo>> {
        return editorsChoiceRepo.getList().cachedIn(viewModelScope)
    }
    
    fun loadEditorsChoiceContent(): Flow<PagingData<ChannelInfo>> {
        return editorsChoiceRepo.getList()
    }
    
    fun loadUserChannels(): Flow<PagingData<UserChannelInfo>> {
        return userChannelRepo.getList().cachedIn(viewModelScope)
    }

    private val userChannelRepo by lazy {
        BaseListRepositoryImpl({
            BaseNetworkPagingSource(
                popularChannelAssistedFactory.create(
                    ApiCategoryRequestParams("", 0, 0)
                ), ApiNames.GET_POPULAR_TV_CHANNEL, pageName.value ?: BrowsingScreens.HOME_PAGE
            )
        })
    }

    fun loadUserChannelsByCategory(category: Category): Flow<PagingData<UserChannelInfo>> {
        return BaseListRepositoryImpl({
            BaseNetworkPagingSource(
                popularChannelAssistedFactory.create(
                    ApiCategoryRequestParams("", 1, category.id.toInt())
                ), ApiNames.GET_CONTENTS_V5, pageName.value ?: BrowsingScreens.HOME_PAGE
            )
        }).getList().cachedIn(viewModelScope)
    }

    private val channelRepo by lazy {
        BaseListRepositoryImpl({
            BaseNetworkPagingSource(
                getContentAssistedFactory.create(
                    ChannelRequestParams("", 0, "", 0, "LIVE")
                ), ApiNames.GET_CONTENTS_V5, pageName.value ?: BrowsingScreens.HOME_PAGE
            )
        })
    }

    private val categoryListRepo by lazy {
        BaseListRepositoryImpl({
            BaseNetworkPagingSource(
                categoryListApi, ApiNames.GET_CATEGORIES, pageName.value ?: BrowsingScreens.HOME_PAGE
            )
        })
    }

    fun loadLatestVideos(): Flow<PagingData<ChannelInfo>> {
        return BaseListRepositoryImpl({
            BaseNetworkPagingSource(
                getContentAssistedFactory.create(
                    ChannelRequestParams("", 0, "", 0, "VOD")
                ), ApiNames.GET_CONTENTS_V5, pageName.value ?: BrowsingScreens.HOME_PAGE
            )
        }).getList().cachedIn(viewModelScope)
    }

    fun loadMostPopularVideos(categoryId: Int, subCategoryId: Int): Flow<PagingData<ChannelInfo>> {
        return BaseListRepositoryImpl({
            BaseNetworkPagingSource(
                mostPopularApi.create(
                    LandingUserChannelsRequestParam("VOD", categoryId, subCategoryId, isDramaSeries.value ?: false)
                ), ApiNames.GET_MOST_POPULAR_CONTENTS, pageName.value ?: BrowsingScreens.HOME_PAGE
            )
        }).getList()
    }

    private val editorsChoiceRepo by lazy {
        BaseListRepositoryImpl({
            BaseNetworkPagingSource(
                editorsChoiceAssistedFactory.create(
                    EditorsChoiceFeaturedRequestParams("VOD", pageType.value ?: Landing, categoryId.value ?: 0)
                ), ApiNames.GET_EDITOR_CHOICE, pageName.value ?: BrowsingScreens.HOME_PAGE
            )
        })
    }

    fun loadLatestVideosByCategory(categoryId: Int, subCategoryId: Int): Flow<PagingData<ChannelInfo>> {
        return BaseListRepositoryImpl({
            BaseNetworkPagingSource(
                getContentAssistedFactory.create(
                    ChannelRequestParams("", categoryId, "", subCategoryId, "VOD")
                ), ApiNames.GET_CONTENTS_V5, pageName.value ?: BrowsingScreens.HOME_PAGE
            )
        }).getList()
    }

    val loadPopularMovieChannels by lazy {
        BaseListRepositoryImpl({
            tvChannelRepo.getPopularMovieChannels()
        }).getList()
    }
    
    fun loadPopularMovieChannelsCount() {
        viewModelScope.launch { 
            moviesChannelCount.postValue(tvChannelRepo.getPopularMovieChannelsCount())
        }
    }
    
    fun loadHashTagContents(hashTag: String, categoryId: Int, subCategoryId: Int): Flow<PagingData<ChannelInfo>> {
        return BaseListRepositoryImpl({
            BaseNetworkPagingSource(
                relativeContentsFactory.create(
                    CatchupParams("null", hashTag, categoryId, subCategoryId)
                ), ApiNames.GET_RELATIVE_CONTENTS, pageName.value ?: BrowsingScreens.HOME_PAGE
            )
        }).getList()
    }
    
    val loadFeaturedPartners by lazy {
        BaseListRepositoryImpl({
            BaseNetworkPagingSource(
                featuredPartnerAssistedFactory.create("VOD"), ApiNames.GET_FEATURED_PARTNERS, BrowsingScreens.HOME_PAGE
            )
        }).getList()
    }
}