package com.banglalink.toffee.ui.challenge

import androidx.hilt.lifecycle.ViewModelInject
import com.banglalink.toffee.apiservice.GetChallenges
import com.banglalink.toffee.common.paging.BaseListRepository
import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.common.paging.BaseNetworkPagingSource
import com.banglalink.toffee.common.paging.BasePagingViewModel
import com.banglalink.toffee.model.Challenge
import com.banglalink.toffee.model.ChannelInfo

class ChallengeViewModel @ViewModelInject constructor(apiService: GetChallenges): BasePagingViewModel<Challenge>() {
    override val repo: BaseListRepository<Challenge> by lazy {
        BaseListRepositoryImpl(BaseNetworkPagingSource(apiService))
    }
}