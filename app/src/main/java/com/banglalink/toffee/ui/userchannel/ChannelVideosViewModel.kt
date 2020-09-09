package com.banglalink.toffee.ui.userchannel

import com.banglalink.toffee.data.network.retrofit.RetrofitApiClient
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.ChannelVideo
import com.banglalink.toffee.ui.common.SingleListRepository
import com.banglalink.toffee.ui.common.SingleListViewModel
import com.banglalink.toffee.usecase.GetChannelVideos

class ChannelVideosViewModel: SingleListViewModel<ChannelVideo>() {
    override var repo: SingleListRepository<ChannelVideo>  = GetChannelVideos(Preference.getInstance(), RetrofitApiClient.toffeeApi)
    
    //override var enableToolbar: Boolean = false
}