package com.banglalink.toffee.ui.category.webseries

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagingData
import com.banglalink.toffee.apiservice.ApiNames
import com.banglalink.toffee.apiservice.BrowsingScreens
import com.banglalink.toffee.apiservice.DramaSeasonRequestParam
import com.banglalink.toffee.apiservice.GetDramaEpisodesBySeasonService
import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.common.paging.BaseNetworkPagingSource
import com.banglalink.toffee.model.ChannelInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class EpisodeListViewModel @Inject constructor(
    private val episodeListApi: GetDramaEpisodesBySeasonService.AssistedFactory,
) : ViewModel() {
    var seasonList = MutableLiveData<List<String>>()
    var selectedSeason = MutableLiveData<Int>()

    fun getEpisodesBySeason(params: DramaSeasonRequestParam): Flow<PagingData<ChannelInfo>> {
        return BaseListRepositoryImpl({
            BaseNetworkPagingSource(
                episodeListApi.create(params), ApiNames.GET_WEB_SERIES_BY_SEASON, BrowsingScreens.WEB_SERIES_EPISODE_LIST_PAGE
            )
        }).getList()
    }
}