package com.banglalink.toffee.ui.mychannel

import androidx.paging.PagingData
import com.banglalink.toffee.apiservice.MyChannelPlaylistService
import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.common.paging.BaseNetworkPagingSource
import com.banglalink.toffee.model.MyChannelPlaylist
import com.banglalink.toffee.ui.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class MyChannelPlaylistViewModel @Inject constructor(
    private val apiService: MyChannelPlaylistService.AssistedFactory, 
) : BaseViewModel() {

    fun getMyChannelPlaylists(channelOwnerId: Int): Flow<PagingData<MyChannelPlaylist>> {
        return BaseListRepositoryImpl({
            BaseNetworkPagingSource(
                apiService.create(channelOwnerId)
            )
        }).getList()
    }
}