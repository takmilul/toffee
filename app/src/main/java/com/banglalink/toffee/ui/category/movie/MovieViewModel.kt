package com.banglalink.toffee.ui.category.movie

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.apiservice.*
import com.banglalink.toffee.data.network.request.ChannelRequestParams
import com.banglalink.toffee.data.repository.ContentViewPorgressRepsitory
import com.banglalink.toffee.data.repository.ContinueWatchingRepository
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.ComingSoonContent
import com.banglalink.toffee.model.MoviesContentVisibilityCards
import com.banglalink.toffee.ui.common.BaseViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MovieViewModel @ViewModelInject constructor(
    private val movieApiService: MovieCategoryDetailService,
    private val moviePreviewsService: MoviesPreviewService,
    private val trendingNowService: GetMostPopularContents.AssistedFactory,
    private val viewProgressRepo: ContentViewPorgressRepsitory,
    private val continueWatchingRepo: ContinueWatchingRepository,
    private val getContentAssistedFactory: GetContents.AssistedFactory,
    private val comingSoonApiService: MoviesComingSoonService,
): BaseViewModel() {
    
    private val moviesContentCardsResponse = MutableLiveData<MoviesContentVisibilityCards>()
    val moviesContentCards = moviesContentCardsResponse.toLiveData()
    private val thrillerMoviesResponse = MutableLiveData<List<ChannelInfo>>()
    val thrillerMovies = thrillerMoviesResponse.toLiveData()
    private val actionMoviesResponse = MutableLiveData<List<ChannelInfo>>()
    val actionMovies = actionMoviesResponse.toLiveData()
    private val romanticMoviesResponse = MutableLiveData<List<ChannelInfo>>()
    val romanticMovies = romanticMoviesResponse.toLiveData()
    private val banglaMoviesResponse = MutableLiveData<List<ChannelInfo>>()
    val banglaMovies = banglaMoviesResponse.toLiveData()
    private val englishMoviesResponse = MutableLiveData<List<ChannelInfo>>()
    val englishMovies = englishMoviesResponse.toLiveData()
    private val moviePreviewsResponse = MutableLiveData<List<ChannelInfo>>()
    val moviePreviews = moviePreviewsResponse.toLiveData()
    private val trendingNowMoviesResponse = MutableLiveData<List<ChannelInfo>>()
    val trendingNowMovies = trendingNowMoviesResponse.toLiveData()
    private val telefilmsResponse = MutableLiveData<List<ChannelInfo>>()
    val telefilms = telefilmsResponse.toLiveData()
    private val comingSoonResponse = MutableLiveData<List<ComingSoonContent>>()
    val comingSoonContents = comingSoonResponse.toLiveData()
    private var isContinueWatchingDbInitialized = false
    private var continueWatchingCount = 0
    
    val loadMovieCategoryDetail by lazy{
        viewModelScope.launch {
            val response = try {
                movieApiService.loadData("VOD", 0, 0)
            } catch (ex: Exception) {
                null
            }
            moviesContentCardsResponse.value = response?.cards?.let { card ->
                card.continueWatching = if (isContinueWatchingDbInitialized) continueWatchingCount else card.continueWatching
                card.moviePreviews = moviePreviewsResponse.value?.let{ if (it.isEmpty()) 0 else card.moviePreviews } ?: 0 
                card.trendingNow = trendingNowMoviesResponse.value?.let{ if (it.isEmpty()) 0 else card.trendingNow } ?: 0 
                card.telefilm = telefilmsResponse.value?.let{ if (it.isEmpty()) 0 else card.telefilm } ?: 0 
                card.comingSoon = comingSoonResponse.value?.let{ if (it.isEmpty()) 0 else card.comingSoon } ?: 0 
                
                card
            } ?: MoviesContentVisibilityCards()

            thrillerMoviesResponse.value = response?.subCategoryWiseContent?.find { it.subCategoryName == "Thriller" }?.let {
                it.channels?.map { cinfo->
                    cinfo.categoryId = 1
                    cinfo.viewProgress = viewProgressRepo.getProgressByContent(cinfo.id.toLong())?.progress ?: 0L
                    cinfo
                }
            } ?: kotlin.run {
                moviesContentCardsResponse.value = moviesContentCardsResponse.value?.apply {
                    thriller = 0
                }
                emptyList()
            }

            actionMoviesResponse.value = response?.subCategoryWiseContent?.find { it.subCategoryName == "Action" }?.let {
                it.channels?.map { cinfo->
                    cinfo.categoryId = 1
                    cinfo.viewProgress = viewProgressRepo.getProgressByContent(cinfo.id.toLong())?.progress ?: 0L
                    cinfo
                }
            } ?: kotlin.run {
                moviesContentCardsResponse.value = moviesContentCardsResponse.value?.apply {
                    action = 0
                }
                emptyList()
            }

            romanticMoviesResponse.value = response?.subCategoryWiseContent?.find { it.subCategoryName == "Romance" }?.let {
                it.channels?.map { cinfo->
                    cinfo.categoryId = 1
                    cinfo.viewProgress = viewProgressRepo.getProgressByContent(cinfo.id.toLong())?.progress ?: 0L
                    cinfo
                }
            } ?: kotlin.run {
                moviesContentCardsResponse.value = moviesContentCardsResponse.value?.apply {
                    romantic = 0
                }
                emptyList()
            }

            banglaMoviesResponse.value = response?.subCategoryWiseContent?.find { it.subCategoryName == "Bangla" }?.let {
                it.channels?.map { cinfo->
                    cinfo.categoryId = 1
                    cinfo.viewProgress = viewProgressRepo.getProgressByContent(cinfo.id.toLong())?.progress ?: 0L
                    cinfo
                }
            } ?: kotlin.run {
                moviesContentCardsResponse.value = moviesContentCardsResponse.value?.apply {
                    bangla = 0
                }
                emptyList()
            }

            englishMoviesResponse.value = response?.subCategoryWiseContent?.find { it.subCategoryName == "English" }?.let {
                it.channels?.map { cinfo->
                    cinfo.categoryId = 1
                    cinfo.viewProgress = viewProgressRepo.getProgressByContent(cinfo.id.toLong())?.progress ?: 0L
                    cinfo
                }
            } ?: kotlin.run {
                moviesContentCardsResponse.value = moviesContentCardsResponse.value?.apply {
                    english = 0
                }
                emptyList()
            }
        }
    }
    
    val loadMoviePreviews by lazy{
        viewModelScope.launch {
            moviePreviewsResponse.value = try {
                moviePreviewsService.loadData("VOD", 0, 0, 10, 0).map {
                    it.categoryId = 1
                    it.viewProgress = viewProgressRepo.getProgressByContent(it.id.toLong())?.progress ?: 0L
                    it
                }.run { 
                    moviesContentCardsResponse.value = moviesContentCardsResponse.value?.apply {
                        moviePreviews = if (isEmpty()) 0 else moviePreviews
                    }
                    this
                }
            } catch (ex: Exception) {
                moviesContentCardsResponse.value = moviesContentCardsResponse.value?.apply {
                    moviePreviews = 0
                }
                emptyList()
            }
        }
    }
    
    val loadTrendingNowMovies by lazy {
        viewModelScope.launch {
            val response = trendingNowService.create(TrendingNowRequestParam("VOD", 1, 0, false)).loadData(0, 10)
            trendingNowMoviesResponse.value = try {
                response.map {
                    it.categoryId = 1
                    it.viewProgress = viewProgressRepo.getProgressByContent(it.id.toLong())?.progress ?: 0L
                    it
                }.run {
                    moviesContentCardsResponse.value = moviesContentCardsResponse.value?.apply {
                        trendingNow = if(isEmpty()) 0 else trendingNow
                    }
                    this
                }
            }
            catch (ex: Exception) {
                moviesContentCardsResponse.value = moviesContentCardsResponse.value?.apply {
                    trendingNow = 0
                }
                emptyList()
            }
        }
    }

    val loadTelefilms by lazy {
        viewModelScope.launch {
            telefilmsResponse.value =  try{
                getContentAssistedFactory.create(
                    ChannelRequestParams("", 1, "", 90, "VOD")
                ).loadData(0,10).map {
                    it.categoryId = 1
                    it.viewProgress = viewProgressRepo.getProgressByContent(it.id.toLong())?.progress ?: 0L
                    it
                }.run {
                    moviesContentCardsResponse.value = moviesContentCardsResponse.value?.apply {
                        telefilm = if(isEmpty()) 0 else telefilm
                    }
                    this
                }
            } catch (ex: Exception) {
                moviesContentCardsResponse.value = moviesContentCardsResponse.value?.apply {
                    telefilm = 0
                }
                emptyList()
            }
        }
    }

    val loadComingSoonContents by lazy{
        viewModelScope.launch {
            comingSoonResponse.value = try{
                comingSoonApiService.loadData("VOD", 1, 0, 10, 0).run { 
                    moviesContentCardsResponse.value = moviesContentCardsResponse.value?.apply { 
                        comingSoon = if(isEmpty()) 0 else comingSoon
                    }
                    this
                }
            } catch (ex: Exception) {
                moviesContentCardsResponse.value = moviesContentCardsResponse.value?.apply {
                    comingSoon = 0
                }
                emptyList()
            }
        }
    }

    fun getContinueWatchingFlow(catId: Int): Flow<List<ChannelInfo>> {
        return continueWatchingRepo.getAllItemsByCategory(catId).map {
            isContinueWatchingDbInitialized = true
            continueWatchingCount = it.size
            it.mapNotNull { item ->
                item.channelInfo
            }.apply {
                moviesContentCardsResponse.value = moviesContentCardsResponse.value?.also { cardList ->
                    cardList.continueWatching = if (isEmpty()) 0 else cardList.continueWatching
                }
            }
        }
    }
}