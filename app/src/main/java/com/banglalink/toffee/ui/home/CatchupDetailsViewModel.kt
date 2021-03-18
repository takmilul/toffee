package com.banglalink.toffee.ui.home

import androidx.paging.PagingData
import com.banglalink.toffee.apiservice.CatchupParams
import com.banglalink.toffee.apiservice.GetRelativeContents
import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.common.paging.BaseNetworkPagingSource
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class CatchupDetailsViewModel @Inject constructor(
    private val relativeContentsFactory: GetRelativeContents.AssistedFactory,
) : BaseViewModel() {
    
    fun loadRelativeContent(catchupParams: CatchupParams): Flow<PagingData<ChannelInfo>> {
        return BaseListRepositoryImpl({
            BaseNetworkPagingSource(relativeContentsFactory.create(catchupParams))
        }).getList()
    }
}