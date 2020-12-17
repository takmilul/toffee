package com.banglalink.toffee.ui.category.drama

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.banglalink.toffee.apiservice.DramaSeriesContentService
import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.common.paging.BaseNetworkPagingSource
import com.banglalink.toffee.data.network.request.ChannelRequestParams
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.BaseViewModel
import kotlinx.coroutines.flow.Flow

class DramaSeriesViewModel @ViewModelInject constructor(
    private val dramaAssistedFactory: DramaSeriesContentService.AssistedFactory,
) : BaseViewModel() {

    fun loadDramaSeriesContents(categoryId: Int, subCategoryId: Int): Flow<PagingData<ChannelInfo>> {
        return BaseListRepositoryImpl({ 
            BaseNetworkPagingSource(
                dramaAssistedFactory.create(
                    ChannelRequestParams("", categoryId, "", subCategoryId, "VOD")
                )
            ) 
        }).getList()
    }
}