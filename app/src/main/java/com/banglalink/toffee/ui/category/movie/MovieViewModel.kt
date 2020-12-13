package com.banglalink.toffee.ui.category.movie

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.apiservice.MovieCategoryDetailService
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.MoviesContentVisibilityCards
import com.banglalink.toffee.ui.common.BaseViewModel
import kotlinx.coroutines.launch

class MovieViewModel @ViewModelInject constructor(
    private val movieApiService: MovieCategoryDetailService
): BaseViewModel() {
    private val movieContentCardsResponse = MutableLiveData<MoviesContentVisibilityCards>()
    val movieContentCards = movieContentCardsResponse.toLiveData()
    private val thrillerContentsResponse = MutableLiveData<List<ChannelInfo>>()
    val thrillerContents = thrillerContentsResponse.toLiveData()
    private val actionContentsResponse = MutableLiveData<List<ChannelInfo>>()
    val actionContents = actionContentsResponse.toLiveData()
    private val romanticContentsResponse = MutableLiveData<List<ChannelInfo>>()
    val romanticContents = romanticContentsResponse.toLiveData()
    
    fun loadMovieCategoryDetail(){
        viewModelScope.launch { 
            val response = movieApiService.loadData("VOD", 0, 0)
            movieContentCardsResponse.value = response.cards
            response.subCategoryWiseContent?.singleOrNull { it.subCategoryName == "Thriller" }?.let {
                thrillerContentsResponse.value = it.channels
            }
            response.subCategoryWiseContent?.singleOrNull { it.subCategoryName == "Action" }?.let {
                actionContentsResponse.value = it.channels
            }
            response.subCategoryWiseContent?.singleOrNull { it.subCategoryName == "Romance" }?.let {
                romanticContentsResponse.value = it.channels
            }
        }
    }
    
}