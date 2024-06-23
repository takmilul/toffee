package com.banglalink.toffee.ui.favorite

import com.banglalink.toffee.apiservice.ApiNames
import com.banglalink.toffee.apiservice.BrowsingScreens
import com.banglalink.toffee.apiservice.GetFavoriteContentsService
import com.banglalink.toffee.common.paging.BaseListRepository
import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.common.paging.BaseNetworkPagingSource
import com.banglalink.toffee.common.paging.BasePagingViewModel
import com.banglalink.toffee.model.ChannelInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FavoriteViewModel @Inject constructor(
    private val apiService: GetFavoriteContentsService
): BasePagingViewModel<ChannelInfo>() {
    
    override fun repo(): BaseListRepository<ChannelInfo> {
        return BaseListRepositoryImpl({
            BaseNetworkPagingSource(apiService, ApiNames.GET_FAVORITE_CONTENTS, BrowsingScreens.FAVORITE_CONTENTS_PAGE)
        })
    }
}