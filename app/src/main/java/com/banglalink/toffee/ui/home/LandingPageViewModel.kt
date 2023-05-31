package com.banglalink.toffee.ui.home

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.banglalink.toffee.apiservice.ApiCategoryRequestParams
import com.banglalink.toffee.apiservice.ApiNames
import com.banglalink.toffee.apiservice.BrowsingScreens
import com.banglalink.toffee.apiservice.CatchupParams
import com.banglalink.toffee.apiservice.FeatureContentService
import com.banglalink.toffee.apiservice.FeaturedPartnerService
import com.banglalink.toffee.apiservice.GetCategories
import com.banglalink.toffee.apiservice.GetContentService
import com.banglalink.toffee.apiservice.GetEditorsChoiceContents
import com.banglalink.toffee.apiservice.GetMostPopularContents
import com.banglalink.toffee.apiservice.GetPopularUserChannels
import com.banglalink.toffee.apiservice.GetRelativeContents
import com.banglalink.toffee.apiservice.LandingUserChannelsRequestParam
import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.common.paging.BaseNetworkPagingSource
import com.banglalink.toffee.data.exception.JobCanceledError
import com.banglalink.toffee.data.network.request.ChannelRequestParams
import com.banglalink.toffee.data.network.util.resultFromResponse
import com.banglalink.toffee.enums.PageType
import com.banglalink.toffee.enums.PageType.Landing
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.Category
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.EditorsChoiceFeaturedRequestParams
import com.banglalink.toffee.model.FeaturedPartner
import com.banglalink.toffee.model.Resource.Failure
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.model.SubCategory
import com.banglalink.toffee.model.UserChannelInfo
import com.banglalink.toffee.usecase.SendFeaturePartnerEvent
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
    @ApplicationContext private val context: Context,
    private val featuredAssistedFactory: FeatureContentService,
    private val getContentAssistedFactory: GetContentService.AssistedFactory,
    private val mostPopularApi: GetMostPopularContents.AssistedFactory,
    private val relativeContentsFactory: GetRelativeContents.AssistedFactory,
    private val popularChannelAssistedFactory: GetPopularUserChannels.AssistedFactory,
    private val featuredPartnerAssistedFactory: FeaturedPartnerService.AssistedFactory,
    private val editorsChoiceAssistedFactory: GetEditorsChoiceContents.AssistedFactory,
    private val sendFeaturePartnerEvent: SendFeaturePartnerEvent,
) : ViewModel() {
    
    var featuredJob: Job? = null
    val categoryId = SingleLiveEvent<Int>()
    val pageName = MutableLiveData<String>()
    val subCategoryId = SingleLiveEvent<Int>()
    val pageType = MutableLiveData<PageType>()
    val isDramaSeries = MutableLiveData<Boolean>()
    val selectedHashTag = SingleLiveEvent<String>()
    val featuredPageName = MutableLiveData<String>()
    val hashtagList = SingleLiveEvent<List<String>>()
    val checkedSubCategoryChipId = MutableLiveData<Int>()
    val subCategories = SingleLiveEvent<List<SubCategory>>()
    val selectedCategory = MutableLiveData<Category>()
    val featuredContents = SingleLiveEvent<List<ChannelInfo>>()
    val featuredPartnerDeeplinkLiveData = SingleLiveEvent<FeaturedPartner>()
    
    fun loadChannels(): Flow<PagingData<ChannelInfo>> {
        return BaseListRepositoryImpl({
            BaseNetworkPagingSource(
                getContentAssistedFactory.create(
                    ChannelRequestParams("", 0, "", 0, "LIVE")
                ), ApiNames.GET_CONTENTS_V5, pageName.value ?: BrowsingScreens.HOME_PAGE
            )
        }).getList(10).cachedIn(viewModelScope)
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
        return BaseListRepositoryImpl({
            BaseNetworkPagingSource(
                categoryListApi, ApiNames.GET_CATEGORIES, pageName.value ?: BrowsingScreens.HOME_PAGE
            )
        }).getList().cachedIn(viewModelScope)
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
    
    fun loadLatestVideos(): Flow<PagingData<ChannelInfo>> {
        return BaseListRepositoryImpl({
            BaseNetworkPagingSource(
                getContentAssistedFactory.create(
                    ChannelRequestParams("", 0, "", 0, "VOD")
                ), ApiNames.GET_CONTENTS_V5, pageName.value ?: BrowsingScreens.HOME_PAGE
            )
        }).getList(10).cachedIn(viewModelScope)
    }
    
    fun loadCategoryWiseContent(categoryId: Int): Flow<PagingData<ChannelInfo>> {
        return BaseListRepositoryImpl({
            BaseNetworkPagingSource(
                getContentAssistedFactory.create(
                    ChannelRequestParams("", categoryId, "", 0, "LIVE")
                ), ApiNames.GET_CONTENTS_V5, pageName.value ?: BrowsingScreens.HOME_PAGE
            )
        }).getList(10).cachedIn(viewModelScope)
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
        }).getList(10)
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
    
    fun loadFeaturedPartners (): Flow<PagingData<FeaturedPartner>> {
        return BaseListRepositoryImpl({
            BaseNetworkPagingSource(
                featuredPartnerAssistedFactory.create("VOD"), ApiNames.GET_FEATURED_PARTNERS, BrowsingScreens.HOME_PAGE
            )
        }).getList()
    }
    
    fun loadFeaturedPartnerList (partnerId: Int) {
        viewModelScope.launch {
            val result = resultFromResponse { featuredPartnerAssistedFactory.create("VOD").loadData(0, 30) }
            if (result is Success) {
                featuredPartnerDeeplinkLiveData.postValue(result.data.find { it.id == partnerId })
            }
        }
    }
    
    fun sendFeaturePartnerReportData(partnerName: String,partnerId:Int) {
        viewModelScope.launch {
            sendFeaturePartnerEvent.execute(partnerName,partnerId)
        }
    }
}