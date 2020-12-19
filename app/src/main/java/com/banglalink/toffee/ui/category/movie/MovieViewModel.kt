package com.banglalink.toffee.ui.category.movie

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.apiservice.*
import com.banglalink.toffee.data.network.request.ChannelRequestParams
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.ComingSoonContent
import com.banglalink.toffee.model.MoviesContentVisibilityCards
import com.banglalink.toffee.ui.common.BaseViewModel
import kotlinx.coroutines.launch

class MovieViewModel @ViewModelInject constructor(
    private val movieApiService: MovieCategoryDetailService,
    private val moviePreviewsService: MoviesPreviewService,
    private val trendingNowService: GetMostPopularContents.AssistedFactory,
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
    
    val loadMovieCategoryDetail by lazy{
        viewModelScope.launch { 
            val response = movieApiService.loadData("VOD", 0, 0)
            moviesContentCardsResponse.value = response.cards
            response.subCategoryWiseContent?.singleOrNull { it.subCategoryName == "Thriller" }?.let {
                thrillerMoviesResponse.value = it.channels
            }
            response.subCategoryWiseContent?.singleOrNull { it.subCategoryName == "Action" }?.let {
                actionMoviesResponse.value = it.channels
            }
            response.subCategoryWiseContent?.singleOrNull { it.subCategoryName == "Romance" }?.let {
                romanticMoviesResponse.value = it.channels
            }
            response.subCategoryWiseContent?.singleOrNull { it.subCategoryName == "Bangla" }?.let {
                banglaMoviesResponse.value = it.channels
            }
            response.subCategoryWiseContent?.singleOrNull { it.subCategoryName == "English" }?.let {
                englishMoviesResponse.value = it.channels
            }
        }
    }
    
    val loadMoviePreviews by lazy{
        viewModelScope.launch { 
            val response = moviePreviewsService.loadData("VOD",0,0, 10, 0)
            moviePreviewsResponse.value = response
        }
    }
    
    val loadTrendingNowMovies by lazy{
        viewModelScope.launch { 
            val response = trendingNowService.create(TrendingNowRequestParam("VOD", 1, 0, false)).loadData(0, 10)
            trendingNowMoviesResponse.value = response
        }
    }

    val loadTelefilms by lazy {
        viewModelScope.launch { 
            val response = getContentAssistedFactory.create(
                ChannelRequestParams("", 1, "", 90, "VOD")
            ).loadData(0,10)
            telefilmsResponse.value = response
        }
    }

    val loadComingSoonContents by lazy{
        viewModelScope.launch {
            val response = comingSoonApiService.loadData("VOD", 1, 0, 10, 0)
            comingSoonResponse.value = response
        }
    }
}