package com.banglalink.toffee.ui.userchannel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.banglalink.toffee.apiservice.GetChannelPlaylists
import com.banglalink.toffee.apiservice.MyChannelPlaylistParams
import com.banglalink.toffee.common.paging.BaseListRepository
import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.common.paging.BaseNetworkPagingSource
import com.banglalink.toffee.common.paging.BasePagingViewModel
import com.banglalink.toffee.model.UgcChannelPlaylist
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject

class ChannelPlaylistViewModel @AssistedInject constructor(private val apiService: GetChannelPlaylists.AssistedFactory, @Assisted private val params: MyChannelPlaylistParams) : BasePagingViewModel<UgcChannelPlaylist>() {

    private val dataSource by lazy { BaseNetworkPagingSource(apiService.create(params)) } 
    override val repo: BaseListRepository<UgcChannelPlaylist> by lazy { BaseListRepositoryImpl(dataSource) }

    @AssistedInject.Factory
    interface AssistedFactory {
        fun create(params: MyChannelPlaylistParams): ChannelPlaylistViewModel
    }

    companion object {
        fun provideFactory(assistedFactory: AssistedFactory, params: MyChannelPlaylistParams): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                    return assistedFactory.create(params) as T
                }
            }
    }
    
    fun reloadContent(){
        dataSource.invalidate()
    }

}