package com.banglalink.toffee.ui.challenge

import com.banglalink.toffee.apiservice.GetChallenges
import com.banglalink.toffee.common.paging.BaseListRepository
import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.common.paging.BaseNetworkPagingSource
import com.banglalink.toffee.common.paging.BasePagingViewModel
import com.banglalink.toffee.model.Challenge
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChallengeViewModel @Inject constructor(apiService: GetChallenges): BasePagingViewModel<Challenge>() {
    override val repo: BaseListRepository<Challenge> by lazy {
        BaseListRepositoryImpl( { BaseNetworkPagingSource(apiService) } )
    }
}