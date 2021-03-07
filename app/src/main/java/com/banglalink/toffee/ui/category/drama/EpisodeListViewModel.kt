package com.banglalink.toffee.ui.category.drama

import androidx.lifecycle.MutableLiveData
import androidx.paging.PagingData
import com.banglalink.toffee.apiservice.DramaSeasonRequestParam
import com.banglalink.toffee.apiservice.GetDramaEpisodesBySeason
import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.common.paging.BaseNetworkPagingSource
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class EpisodeListViewModel @Inject constructor(
    private val episodeListApi: GetDramaEpisodesBySeason.AssistedFactory,
) : BaseViewModel() {
    var seasonList = MutableLiveData<List<String>>()
    var selectedSeason = MutableLiveData<Int>()

    fun getEpisodesBySeason(params: DramaSeasonRequestParam): Flow<PagingData<ChannelInfo>> {
        return BaseListRepositoryImpl({
            BaseNetworkPagingSource(episodeListApi.create(
                params
            ))
        }).getList()
    }
}