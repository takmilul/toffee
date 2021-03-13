package com.banglalink.toffee.ui.category.drama

import androidx.paging.PagingData
import com.banglalink.toffee.apiservice.DramaSeriesContentService
import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.common.paging.BaseNetworkPagingSource
import com.banglalink.toffee.data.network.request.ChannelRequestParams
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class DramaSeriesViewModel @Inject constructor(
    private val dramaAssistedFactory: DramaSeriesContentService.AssistedFactory,
) : BaseViewModel() {

    fun loadDramaSeriesContents(categoryId: Int, subCategoryId: Int, isFilter: Int, hashTag: String): Flow<PagingData<ChannelInfo>> {
        return BaseListRepositoryImpl({ 
            BaseNetworkPagingSource(
                dramaAssistedFactory.create(
                    ChannelRequestParams("", categoryId, "", subCategoryId, "VOD", isFilter, hashTag)
                )
            ) 
        }).getList()
    }
}