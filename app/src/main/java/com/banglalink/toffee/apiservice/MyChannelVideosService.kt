package com.banglalink.toffee.apiservice

import com.banglalink.toffee.common.paging.BaseApiService
import com.banglalink.toffee.data.database.dao.ReactionDao
import com.banglalink.toffee.data.network.request.MyChannelVideosRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.setReactionIcon
import com.banglalink.toffee.util.discardZeroFromDuration
import com.banglalink.toffee.util.getFormattedViewsText
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject

data class MyChannelVideosRequestParams(
    val type: String,
    val isOwner: Int,
    val channelId: Int,
    val categoryId: Int,
    val subcategoryId: Int
)

class MyChannelVideosService @AssistedInject constructor(
    private val preference: Preference, 
    private val toffeeApi: ToffeeApi, 
    private val reactionDao: ReactionDao,
    @Assisted private val requestParams: MyChannelVideosRequestParams
) :
    BaseApiService<ChannelInfo> {

    override suspend fun loadData(offset: Int, limit: Int): List<ChannelInfo> {
        val response = tryIO2 {
            toffeeApi.getMyChannelVideos(
                requestParams.type,
                requestParams.isOwner, requestParams.channelId,
                requestParams.categoryId,
                requestParams.subcategoryId,
                limit, offset,
                preference.getDBVersionByApiName("getUgcChannelAllContent"),
                MyChannelVideosRequest(preference.customerId, preference.password, offset, limit)
            )
        }

        if (response.response.channels != null) {
            return response.response.channels.map {
                it.formatted_view_count = getFormattedViewsText(it.view_count)
                it.formattedDuration = discardZeroFromDuration(it.duration)

                val reaction = reactionDao.getReactionByContentId(it.id, preference.customerId)
                it.userReaction = reaction?.reaction ?: 0
                it.userReactionIcon = setReactionIcon(reaction?.reaction ?: 0)

                it
            }
        }
        return listOf()
    }

    @AssistedInject.Factory
    interface AssistedFactory {
        fun create(requestParams: MyChannelVideosRequestParams): MyChannelVideosService
    }
}