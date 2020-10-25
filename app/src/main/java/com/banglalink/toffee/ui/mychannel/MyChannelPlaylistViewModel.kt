package com.banglalink.toffee.ui.mychannel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.banglalink.toffee.apiservice.MyChannelPlaylistParams
import com.banglalink.toffee.apiservice.MyChannelPlaylistService
import com.banglalink.toffee.common.paging.BaseListRepository
import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.common.paging.BaseNetworkPagingSource
import com.banglalink.toffee.common.paging.BasePagingViewModel
import com.banglalink.toffee.model.MyChannelPlaylist
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject

class MyChannelPlaylistViewModel @AssistedInject constructor(private val apiService: MyChannelPlaylistService.AssistedFactory, @Assisted private val params: MyChannelPlaylistParams) :
    BasePagingViewModel<MyChannelPlaylist>() {

    override val repo: BaseListRepository<MyChannelPlaylist> by lazy { BaseListRepositoryImpl({BaseNetworkPagingSource(apiService.create(params))}) }

    val reloadPlaylist = MutableLiveData<Boolean>()
    
    @AssistedInject.Factory
    interface AssistedFactory {
        fun create(params: MyChannelPlaylistParams): MyChannelPlaylistViewModel
    }

    companion object {
        fun provideFactory(assistedFactory: AssistedFactory, params: MyChannelPlaylistParams): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                    return assistedFactory.create(params) as T
                }
            }
    }
}