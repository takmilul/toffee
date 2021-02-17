package com.banglalink.toffee.ui.challenge

import androidx.hilt.lifecycle.ViewModelInject
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.ChallengeReward
import com.banglalink.toffee.ui.common.SingleListRepository
import com.banglalink.toffee.ui.common.SingleListViewModel
import com.banglalink.toffee.usecase.GetChallengeReward

class ChallengeResultRewardWinnerViewModel @ViewModelInject constructor(private val toffeeApi: ToffeeApi): SingleListViewModel<ChallengeReward>() {
    override var repo: SingleListRepository<ChallengeReward> = GetChallengeReward(Preference.getInstance(), toffeeApi)
}