package com.banglalink.toffee.ui.category.drama

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.banglalink.toffee.apiservice.DramaSeriesContentService
import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.common.paging.BaseNetworkPagingSource
import com.banglalink.toffee.data.network.request.ChannelRequestParams
import com.banglalink.toffee.ui.common.BaseViewModel

class DramaSeriesViewModel @ViewModelInject constructor(
    private val dramaAssistedFactory: DramaSeriesContentService.AssistedFactory,
) : BaseViewModel() {

    val loadDramaSeriesContents by lazy {
        dramaSeriesContentsRepo.getList().cachedIn(viewModelScope)
    }

    private val dramaSeriesContentsRepo by lazy {
        BaseListRepositoryImpl({ 
            BaseNetworkPagingSource(
                dramaAssistedFactory.create(
                    ChannelRequestParams("", 9, "", 0, "VOD")
                )
            ) 
        })
    }
}