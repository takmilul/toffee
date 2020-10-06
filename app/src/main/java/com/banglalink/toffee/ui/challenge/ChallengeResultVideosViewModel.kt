package com.banglalink.toffee.ui.challenge

import androidx.hilt.lifecycle.ViewModelInject
import com.banglalink.toffee.apiservice.GetChannelPlaylists
import com.banglalink.toffee.common.paging.BasePagingViewModel
import com.banglalink.toffee.model.ChannelInfo

class ChallengeResultVideosViewModel @ViewModelInject constructor(override val apiService: GetChannelPlaylists) : BasePagingViewModel<ChannelInfo>()