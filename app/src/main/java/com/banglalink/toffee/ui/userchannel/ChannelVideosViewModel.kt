package com.banglalink.toffee.ui.userchannel

import androidx.hilt.lifecycle.ViewModelInject
import com.banglalink.toffee.apiservice.GetChannelVideos
import com.banglalink.toffee.common.paging.BasePagingViewModel
import com.banglalink.toffee.model.ChannelInfo

class ChannelVideosViewModel @ViewModelInject constructor(override val apiService: GetChannelVideos) : BasePagingViewModel<ChannelInfo>()