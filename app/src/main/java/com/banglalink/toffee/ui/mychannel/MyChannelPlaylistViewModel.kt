package com.banglalink.toffee.ui.mychannel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.banglalink.toffee.apiservice.MyChannelPlaylistService
import com.banglalink.toffee.common.paging.BaseListRepository
import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.common.paging.BaseNetworkPagingSource
import com.banglalink.toffee.common.paging.BasePagingViewModel
import com.banglalink.toffee.model.MyChannelPlaylist
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject

class MyChannelPlaylistViewModel @AssistedInject constructor(
    private val apiService: MyChannelPlaylistService.AssistedFactory, 
    @Assisted private val isOwner: Int, 
    @Assisted private val channelOwnerId: Int) : BasePagingViewModel<MyChannelPlaylist>() {

    override val repo: BaseListRepository<MyChannelPlaylist> by lazy {
        BaseListRepositoryImpl({ BaseNetworkPagingSource(apiService.create(isOwner, channelOwnerId)) }) }

//    val reloadPlaylist = MutableLiveData<Boolean>()

    @AssistedInject.Factory
    interface AssistedFactory {
        fun create(isOwner: Int, channelOwnerId: Int): MyChannelPlaylistViewModel
    }

    companion object {
        fun provideFactory(assistedFactory: AssistedFactory, isOwner: Int, channelOwnerId: Int): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                    Log.i("UGC_Playlist_Service", "UGC_ViewModel -- isOwner: ${isOwner}, ownerId: ${channelOwnerId}")
                    return assistedFactory.create(isOwner, channelOwnerId) as T
                }
            }
    }
}