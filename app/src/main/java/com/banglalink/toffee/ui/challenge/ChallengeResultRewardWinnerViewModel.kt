package com.banglalink.toffee.ui.challenge

import com.banglalink.toffee.model.ChallengeReward
import com.banglalink.toffee.ui.common.SingleListRepository
import com.banglalink.toffee.ui.common.SingleListViewModel
import com.banglalink.toffee.usecase.GetChallengeReward

class ChallengeResultRewardWinnerViewModel: SingleListViewModel<ChallengeReward>() {
    override var repo: SingleListRepository<ChallengeReward> = GetChallengeReward()
}