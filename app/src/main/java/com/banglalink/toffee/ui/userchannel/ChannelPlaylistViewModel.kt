package com.banglalink.toffee.ui.userchannel

import com.banglalink.toffee.data.network.retrofit.RetrofitApiClient
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.ChannelPlaylist
import com.banglalink.toffee.ui.common.SingleListRepository
import com.banglalink.toffee.ui.common.SingleListViewModel
import com.banglalink.toffee.usecase.GetChannelPlaylists

class ChannelPlaylistViewModel: SingleListViewModel<ChannelPlaylist>() {
    override var repo: SingleListRepository<ChannelPlaylist>  = GetChannelPlaylists(Preference.getInstance(), RetrofitApiClient.toffeeApi)
    
}