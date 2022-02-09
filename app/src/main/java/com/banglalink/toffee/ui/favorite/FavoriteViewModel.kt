package com.banglalink.toffee.ui.favorite

import com.banglalink.toffee.apiservice.ApiNames
import com.banglalink.toffee.apiservice.BrowsingScreens
import com.banglalink.toffee.apiservice.GetFavoriteContents
import com.banglalink.toffee.common.paging.BaseListRepository
import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.common.paging.BaseNetworkPagingSource
import com.banglalink.toffee.common.paging.BasePagingViewModel
import com.banglalink.toffee.model.ChannelInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FavoriteViewModel @Inject constructor(
    apiService: GetFavoriteContents
): BasePagingViewModel<ChannelInfo>() {

    override val repo: BaseListRepository<ChannelInfo> by lazy {
        BaseListRepositoryImpl({
            BaseNetworkPagingSource(apiService, ApiNames.GET_FAVORITE_CONTENTS, BrowsingScreens.FAVORITE_CONTENTS_PAGE)
        })
    }
}