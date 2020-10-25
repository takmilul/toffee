package com.banglalink.toffee.ui.mychannel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.banglalink.toffee.apiservice.MyChannelPlaylistContentParam
import com.banglalink.toffee.apiservice.MyChannelPlaylistVideosService
import com.banglalink.toffee.common.paging.BaseListRepository
import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.common.paging.BaseNetworkPagingSource
import com.banglalink.toffee.common.paging.BasePagingViewModel
import com.banglalink.toffee.model.ChannelInfo
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject

class MyChannelPlaylistVideosViewModel @AssistedInject constructor(apiService: MyChannelPlaylistVideosService.AssistedFactory, @Assisted requestParams: MyChannelPlaylistContentParam) :
    BasePagingViewModel<ChannelInfo>() {
    override val repo: BaseListRepository<ChannelInfo> by lazy {
        BaseListRepositoryImpl({ BaseNetworkPagingSource(apiService.create(requestParams)) })
    }

    @AssistedInject.Factory
    interface AssistedFactory {
        fun create(requestParams: MyChannelPlaylistContentParam): MyChannelPlaylistVideosViewModel
    }

    companion object {
        fun provideAssisted(assistedFactory: AssistedFactory, requestParams: MyChannelPlaylistContentParam): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                    return assistedFactory.create(requestParams) as T
                }
            }


    }
}