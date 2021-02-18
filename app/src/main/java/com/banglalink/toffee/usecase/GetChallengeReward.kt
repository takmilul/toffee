package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.ContentRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.ChallengeReward
import com.banglalink.toffee.ui.common.SingleListRepository
import javax.inject.Inject

class GetChallengeReward @Inject constructor(private val preference: Preference, private val toffeeApi: ToffeeApi) : SingleListRepository<ChallengeReward> {

    var mOffset: Int = 0
        private set
    private val limit = 10

    override suspend fun execute(): List<ChallengeReward> {
        val response = tryIO2 {
            toffeeApi.getContents(
                "VOD",
                0, 0,
                mOffset, 30,
                preference.getDBVersionByApiName("getUgcContentsV5"),
                ContentRequest(
                    0,
                    0,
                    "VOD",
                    preference.customerId,
                    preference.password,
                    offset = mOffset,
                    limit = limit
                )
            )
        }

        if (response.response.channels != null) {
            /*return response.response.channels.map {
                it.formatted_view_count = getFormattedViewsText(it.view_count)
                it.formattedDuration = discardZeroFromDuration(it.duration)
                it
            }*/

            val rewardList = mutableListOf<ChallengeReward>()

            rewardList.add(ChallengeReward("", "1st Prize", "iPhone X", "", "Abir87"))
            rewardList.add(ChallengeReward("", "2nd Prize", "GoPro Hero 5 Bla...", "", "AsianStar..."))
            rewardList.add(ChallengeReward("", "3rd Prize", "iPhone X", "", "SundarPichai"))
            rewardList.add(ChallengeReward("", "4th Prize", "iPhone X", "", "AsianStar..."))
            rewardList.add(ChallengeReward("", "5th Prize", "iPhone X", "", "SundarPichai"))

            return rewardList
        }
        return listOf()
        
    }
}