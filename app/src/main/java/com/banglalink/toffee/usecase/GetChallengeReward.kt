package com.banglalink.toffee.usecase

import com.banglalink.toffee.model.ChallengeReward
import com.banglalink.toffee.ui.common.SingleListRepository

class GetChallengeReward : SingleListRepository<ChallengeReward> {
    override suspend fun execute(): List<ChallengeReward> {
        val rewardList = mutableListOf<ChallengeReward>()

        rewardList.add(ChallengeReward("", "1st Prize", "iPhone X", "", "Abir87"))
        rewardList.add(ChallengeReward("", "2nd Prize", "GoPro Hero 5 Bla...", "", "AsianStar..."))
        rewardList.add(ChallengeReward("", "3rd Prize", "iPhone X", "", "SundarPichai"))
        rewardList.add(ChallengeReward("", "4th Prize", "iPhone X", "", "AsianStar..."))
        rewardList.add(ChallengeReward("", "5th Prize", "iPhone X", "", "SundarPichai"))

        return rewardList
    }
}