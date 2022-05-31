package com.banglalink.toffee.ui.mychannel

import androidx.lifecycle.ViewModel
import androidx.paging.PagingData
import com.banglalink.toffee.apiservice.ApiNames
import com.banglalink.toffee.apiservice.BrowsingScreens
import com.banglalink.toffee.apiservice.MyChannelPlaylistService
import com.banglalink.toffee.apiservice.UserPlaylistService
import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.common.paging.BaseNetworkPagingSource
import com.banglalink.toffee.model.MyChannelPlaylist
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class MyChannelPlaylistViewModel @Inject constructor(
    private val apiService: MyChannelPlaylistService.AssistedFactory,
    private val userPlaylistService: UserPlaylistService.AssistedFactory,
) : ViewModel() {

    fun getMyChannelPlaylists(channelOwnerId: Int): Flow<PagingData<MyChannelPlaylist>> {
        return BaseListRepositoryImpl({
            BaseNetworkPagingSource(
                apiService.create(channelOwnerId), ApiNames.GET_PLAYLISTS, BrowsingScreens.MY_CHANNEL_PLAYLIST_PAGE
            )
        }).getList()
    }

    fun getMyChannelUserPlaylists(channelOwnerId: Int): Flow<PagingData<MyChannelPlaylist>> {
        return BaseListRepositoryImpl({
            BaseNetworkPagingSource(
                userPlaylistService.create(channelOwnerId), ApiNames.GET_USER_CHANNEL_PLAYLISTS, BrowsingScreens.USER_PLAYLIST_PAGE
            )
        }).getList()
    }
}