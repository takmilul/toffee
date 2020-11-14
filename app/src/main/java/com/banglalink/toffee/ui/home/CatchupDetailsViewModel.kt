package com.banglalink.toffee.ui.home

import androidx.hilt.lifecycle.ViewModelInject
import androidx.paging.PagingData
import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.common.paging.BaseNetworkPagingSource
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.BaseViewModel
import com.banglalink.toffee.usecase.CatchupParams
import com.banglalink.toffee.usecase.GetRelativeContents
import kotlinx.coroutines.flow.Flow

class CatchupDetailsViewModel @ViewModelInject constructor(
    private val relativeContentsFactory: GetRelativeContents.AssistedFactory
):BaseViewModel() {

    fun loadRelativeContent(channelInfo: ChannelInfo): Flow<PagingData<ChannelInfo>> {
        return BaseListRepositoryImpl({
            BaseNetworkPagingSource(
                relativeContentsFactory.create(
                    CatchupParams(channelInfo.id, channelInfo.video_tags)
                )
            )
        }).getList()
    }
}