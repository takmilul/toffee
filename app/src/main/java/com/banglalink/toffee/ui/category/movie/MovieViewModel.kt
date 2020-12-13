package com.banglalink.toffee.ui.category.movie

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.apiservice.GetContents
import com.banglalink.toffee.apiservice.GetMostPopularContents
import com.banglalink.toffee.apiservice.MovieCategoryDetailService
import com.banglalink.toffee.apiservice.MoviesPreviewService
import com.banglalink.toffee.data.network.request.ChannelRequestParams
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.MoviesContentVisibilityCards
import com.banglalink.toffee.ui.common.BaseViewModel
import com.banglalink.toffee.util.Utils
import com.banglalink.toffee.util.discardZeroFromDuration
import com.banglalink.toffee.util.getFormattedViewsText
import kotlinx.coroutines.launch

class MovieViewModel @ViewModelInject constructor(
    private val movieApiService: MovieCategoryDetailService,
    private val moviePreviewsService: MoviesPreviewService,
    private val trendingNowService: GetMostPopularContents,
    private val getContentAssistedFactory: GetContents.AssistedFactory
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
    
    fun loadMovieCategoryDetail(){
        viewModelScope.launch { 
            val response = movieApiService.loadData("VOD", 0, 0)
            moviesContentCardsResponse.value = response.cards
            response.subCategoryWiseContent?.singleOrNull { it.subCategoryName == "Thriller" }?.let {

                if (it.channels != null) {
                    it.channels.map {
                        it.formatted_view_count = getFormattedViewsText(it.view_count)
                        it.formattedDuration = discardZeroFromDuration(it.duration)

                        if(!it.created_at.isNullOrEmpty()) {
                            it.formattedCreateTime = Utils.getDateDiffInDayOrHourOrMinute(Utils.getDate(it.created_at).time).replace(" ", "")
                        }
                        it.formattedSubscriberCount = getFormattedViewsText(it.subscriberCount.toString())
                        it
                    }
                }
                thrillerMoviesResponse.value = it.channels
            }
            response.subCategoryWiseContent?.singleOrNull { it.subCategoryName == "Action" }?.let {

                if (it.channels != null) {
                    it.channels.map {
                        it.formatted_view_count = getFormattedViewsText(it.view_count)
                        it.formattedDuration = discardZeroFromDuration(it.duration)

                        if(!it.created_at.isNullOrEmpty()) {
                            it.formattedCreateTime = Utils.getDateDiffInDayOrHourOrMinute(Utils.getDate(it.created_at).time).replace(" ", "")
                        }
                        it.formattedSubscriberCount = getFormattedViewsText(it.subscriberCount.toString())
                        it
                    }
                }
                actionMoviesResponse.value = it.channels
            }
            response.subCategoryWiseContent?.singleOrNull { it.subCategoryName == "Romance" }?.let {

                if (it.channels != null) {
                    it.channels.map {
                        it.formatted_view_count = getFormattedViewsText(it.view_count)
                        it.formattedDuration = discardZeroFromDuration(it.duration)

                        if(!it.created_at.isNullOrEmpty()) {
                            it.formattedCreateTime = Utils.getDateDiffInDayOrHourOrMinute(Utils.getDate(it.created_at).time).replace(" ", "")
                        }
                        it.formattedSubscriberCount = getFormattedViewsText(it.subscriberCount.toString())
                        it
                    }
                }
                romanticMoviesResponse.value = it.channels
            }
            response.subCategoryWiseContent?.singleOrNull { it.subCategoryName == "Bangla" }?.let {

                if (it.channels != null) {
                    it.channels.map {
                        it.formatted_view_count = getFormattedViewsText(it.view_count)
                        it.formattedDuration = discardZeroFromDuration(it.duration)

                        if(!it.created_at.isNullOrEmpty()) {
                            it.formattedCreateTime = Utils.getDateDiffInDayOrHourOrMinute(Utils.getDate(it.created_at).time).replace(" ", "")
                        }
                        it.formattedSubscriberCount = getFormattedViewsText(it.subscriberCount.toString())
                        it
                    }
                }
                banglaMoviesResponse.value = it.channels
            }
            response.subCategoryWiseContent?.singleOrNull { it.subCategoryName == "English" }?.let {

                if (it.channels != null) {
                    it.channels.map {
                        it.formatted_view_count = getFormattedViewsText(it.view_count)
                        it.formattedDuration = discardZeroFromDuration(it.duration)

                        if(!it.created_at.isNullOrEmpty()) {
                            it.formattedCreateTime = Utils.getDateDiffInDayOrHourOrMinute(Utils.getDate(it.created_at).time).replace(" ", "")
                        }
                        it.formattedSubscriberCount = getFormattedViewsText(it.subscriberCount.toString())
                        it
                    }
                }
                englishMoviesResponse.value = it.channels
            }
        }
    }
    
    fun loadMoviePreviews(){
        viewModelScope.launch { 
            val response = moviePreviewsService.loadData("VOD",0,0, 10, 0)
            moviePreviewsResponse.value = response
        }
    }
    
    fun loadTrendingNowMovies(){
        viewModelScope.launch { 
            val response = trendingNowService.loadData( 0, 10)
            trendingNowMoviesResponse.value = response
        }
    }

    fun loadTelefilms() {
        viewModelScope.launch { 
            val response = getContentAssistedFactory.create(
                ChannelRequestParams("", 1, "", 90, "VOD")
            ).loadData(0,10)
            telefilmsResponse.value = response
        }
    }

}