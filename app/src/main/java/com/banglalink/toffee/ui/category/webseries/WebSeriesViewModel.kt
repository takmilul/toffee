package com.banglalink.toffee.ui.category.webseries

import androidx.lifecycle.ViewModel
import androidx.paging.PagingData
import com.banglalink.toffee.apiservice.ApiNames
import com.banglalink.toffee.apiservice.BrowsingScreens
import com.banglalink.toffee.apiservice.DramaSeriesContentService
import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.common.paging.BaseNetworkPagingSource
import com.banglalink.toffee.data.network.request.ChannelRequestParams
import com.banglalink.toffee.model.ChannelInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class WebSeriesViewModel @Inject constructor(
    private val dramaAssistedFactory: DramaSeriesContentService.AssistedFactory,
) : ViewModel() {

    fun loadDramaSeriesContents(categoryId: Int, subCategoryId: Int, isFilter: Int, hashTag: String): Flow<PagingData<ChannelInfo>> {
        return BaseListRepositoryImpl({ 
            BaseNetworkPagingSource(
                dramaAssistedFactory.create(
                    ChannelRequestParams("", categoryId, "", subCategoryId, "VOD", isFilter, hashTag)
                ), ApiNames.GET_WEB_SERIES_CONTENT, BrowsingScreens.WEB_SERIES_CATEGORY_PAGE
            )
        }).getList()
    }
}