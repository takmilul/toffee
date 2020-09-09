package com.banglalink.toffee.ui.userchannel

import com.banglalink.toffee.data.network.retrofit.RetrofitApiClient
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.ChannelVideo
import com.banglalink.toffee.ui.common.SingleListRepository
import com.banglalink.toffee.ui.common.SingleListViewModel
import com.banglalink.toffee.usecase.GetChannelPlaylistVideos

class ChannelPlaylistVideosViewModel: SingleListViewModel<ChannelVideo>() {
    override var repo: SingleListRepository<ChannelVideo>  = GetChannelPlaylistVideos(Preference.getInstance(), RetrofitApiClient.toffeeApi)
    
}