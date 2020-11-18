package com.banglalink.toffee.apiservice

import com.banglalink.toffee.common.paging.BaseApiService
import com.banglalink.toffee.data.database.dao.ReactionDao
import com.banglalink.toffee.data.network.request.ChannelRequestParams
import com.banglalink.toffee.data.network.request.ContentRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.enums.Reaction
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.util.Utils
import com.banglalink.toffee.util.discardZeroFromDuration
import com.banglalink.toffee.util.getFormattedViewsText
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject

class GetContents @AssistedInject constructor(
    private val preference: Preference,
    private val toffeeApi: ToffeeApi,
    private val reactionDao: ReactionDao,
//    private val favoriteDao: FavoriteItemDao,
    @Assisted private val requestParams: ChannelRequestParams
): BaseApiService<ChannelInfo> {

    override suspend fun loadData(offset: Int, limit: Int): List<ChannelInfo> {
        val response = tryIO2 {
            toffeeApi.getContents(
                requestParams.type,
                requestParams.categoryId,
                requestParams.subcategoryId,
                offset,
                limit,
                preference.getDBVersionByApiName("getContentsV5"),
                ContentRequest(
                    requestParams.categoryId,
                    requestParams.subcategoryId,
                    requestParams.type,
                    preference.customerId,
                    preference.password,
                    offset = offset,
                    limit = limit
                )
            )
        }

        if (response.response.channels != null) {
            return response.response.channels.map {
                it.category = requestParams.category
                it.subCategoryId = requestParams.subcategoryId
                it.subCategory = requestParams.subcategory
                it.formatted_view_count = getFormattedViewsText(it.view_count)
                it.formattedDuration = discardZeroFromDuration(it.duration)
                val reactionInfo = reactionDao.getReactionByContentId(preference.customerId, it.id)
                it.myReaction = reactionInfo?.reaction ?: Reaction.None.value
                if(!it.created_at.isNullOrEmpty()) {
                    it.formattedCreateTime = Utils.getDateDiffInDayOrHourOrMinute(Utils.getDate(it.created_at).time).replace(" ", "")
                }
                it.formattedSubscriberCount = getFormattedViewsText(it.subscriberCount.toString())
                it
            }
        }
        return emptyList()
    }

    @AssistedInject.Factory
    interface AssistedFactory {
        fun create(requestParams: ChannelRequestParams): GetContents
    }
}