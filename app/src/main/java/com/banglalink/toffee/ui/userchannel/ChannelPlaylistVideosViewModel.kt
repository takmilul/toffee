package com.banglalink.toffee.ui.userchannel

import androidx.hilt.lifecycle.ViewModelInject
import com.banglalink.toffee.apiservice.GetChannelPlaylistVideos
import com.banglalink.toffee.common.paging.BasePagingViewModel
import com.banglalink.toffee.model.ChannelInfo

class ChannelPlaylistVideosViewModel @ViewModelInject constructor(override val apiService: GetChannelPlaylistVideos) : BasePagingViewModel<ChannelInfo>()