package com.banglalink.toffee.apiservice

import com.banglalink.toffee.model.RedeemPoints
import com.banglalink.toffee.model.RedeemPointsBean
import com.banglalink.toffee.model.RedeemPointsMsg

class GetRedeemPoints() {
    var mOffset: Int = 0
        private set
    private val limit = 10

    suspend fun execute(): RedeemPointsBean {
        /*val response = tryIO {
            toffeeApi.getHistoryContents(
                HistoryContentRequest(
                    preference.customerId,
                    preference.password,
                    mOffset,
                    limit
                )
            )
        }


        mOffset += response.response.count
        if (response.response.channels != null) {
            return response.response.channels.map {
                it.formatted_view_count = getFormattedViewsText(it.view_count)
                it.formattedDuration = discardZeroFromDuration(it.duration)
                it
            }
        }*/
        val redeemPoints = listOf<RedeemPoints>(
            RedeemPoints("Watch 3 videos from premium channels within 24 hours", "250 points"),
            RedeemPoints("Watch Live TV for 1 hour within 2 days", "300 points"),
            RedeemPoints("Buy Banglalink 3GB Internet Pack (Validity 7 days)", "550 points"),
            RedeemPoints("Watch 3 videos from premium channel within 2 hours", "250 points"),
            RedeemPoints("Watch 3 videos from premium channel within 2 hours", "250 points")
        )
        return RedeemPointsBean(redeemPoints)
    }
    
    suspend fun redeem(): RedeemPointsMsg{
        return RedeemPointsMsg("You redeemed 550 points by watching premium videos.")
    }
}