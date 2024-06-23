package com.banglalink.toffee.ui.home

import androidx.lifecycle.ViewModel
import androidx.paging.PagingData
import com.banglalink.toffee.apiservice.ApiNames
import com.banglalink.toffee.apiservice.BrowsingScreens
import com.banglalink.toffee.apiservice.CatchupParams
import com.banglalink.toffee.apiservice.GetRelativeContentsService
import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.common.paging.BaseNetworkPagingSource
import com.banglalink.toffee.model.ChannelInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class CatchupDetailsViewModel @Inject constructor(
    private val relativeContentsFactory: GetRelativeContentsService.AssistedFactory,
) : ViewModel() {
    
    fun loadRelativeContent(catchupParams: CatchupParams): Flow<PagingData<ChannelInfo>> {
        return BaseListRepositoryImpl({
            BaseNetworkPagingSource(relativeContentsFactory.create(catchupParams), ApiNames.GET_RELATIVE_CONTENTS, BrowsingScreens.PLAYER_PAGE)
        }).getList()
    }
}