package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.MoviesPreviewRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.util.Utils
import com.banglalink.toffee.util.discardZeroFromDuration
import com.banglalink.toffee.util.getFormattedViewsText
import javax.inject.Inject

class MoviesPreviewService @Inject constructor(
    private val preference: Preference,
    private val toffeeApi: ToffeeApi,
) {
    suspend fun loadData(type: String, categoryId: Int, subCategoryId: Int, limit: Int = 0, offset: Int = 0): List<ChannelInfo> {

        val request =  MoviesPreviewRequest(
            preference.customerId,
            preference.password
        )

        val response = tryIO2 {
            toffeeApi.getMoviePreviews(
                type,
                categoryId,
                subCategoryId,
                limit,
                offset,
                preference.getDBVersionByApiName("getUgcMovieCategoryDetails"),
                request
            )
        }

        if (response.response.channels != null) {
            return response.response.channels.map {
                it.formatted_view_count = getFormattedViewsText(it.view_count)
                it.formattedDuration = discardZeroFromDuration(it.duration)

                if(!it.created_at.isNullOrEmpty()) {
                    it.formattedCreateTime = Utils.getDateDiffInDayOrHourOrMinute(Utils.getDate(it.created_at).time).replace(" ", "")
                }
                it.formattedSubscriberCount = getFormattedViewsText(it.subscriberCount.toString())
//                it.userReactionIcon = setReactionIcon(it.myReaction)

                it
            }
        }
        return listOf()
    }
}