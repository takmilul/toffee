package com.banglalink.toffee.ui.challenge

import com.banglalink.toffee.data.network.request.ContentRequest
import com.banglalink.toffee.data.network.retrofit.RetrofitApiClient
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.SingleListRepository
import com.banglalink.toffee.ui.common.SingleListViewModel
import com.banglalink.toffee.usecase.GetChannelPlaylists

class ChallengeResultVideosViewModel: SingleListViewModel<ChannelInfo>() {

    private val contentRequest = ContentRequest(
        0,
        0,
        "VOD",
        Preference.getInstance().customerId,
        Preference.getInstance().password,
        offset = 0
    )

    private val category: String = ""
    private val subCategory: String = ""

    override var repo: SingleListRepository<ChannelInfo>  = GetChannelPlaylists(RetrofitApiClient.toffeeApi, contentRequest, category, subCategory)
}