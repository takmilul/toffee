package com.banglalink.toffee.ui.challenge

import androidx.hilt.lifecycle.ViewModelInject
import com.banglalink.toffee.apiservice.GetChallenges
import com.banglalink.toffee.common.paging.BasePagingViewModel
import com.banglalink.toffee.model.Challenge

class ChallengeViewModel @ViewModelInject constructor(override val apiService: GetChallenges): BasePagingViewModel<Challenge>()