package com.banglalink.toffee.ui.challenge

import com.banglalink.toffee.model.ChallengeReward
import com.banglalink.toffee.ui.common.SingleListRepository
import com.banglalink.toffee.ui.common.SingleListViewModel

class ChallengeResultRewardWinnerViewModel (
    override var repo: SingleListRepository<ChallengeReward>
): SingleListViewModel<ChallengeReward>()