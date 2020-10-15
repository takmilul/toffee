package com.banglalink.toffee.apiservice

import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseApiService
import com.banglalink.toffee.data.database.dao.ReactionDao
import com.banglalink.toffee.data.network.request.ContentRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.enums.Reaction.*
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.util.discardZeroFromDuration
import com.banglalink.toffee.util.getFormattedViewsText
import javax.inject.Inject


class GetChannelVideos @Inject constructor(private val preference: Preference, private val toffeeApi: ToffeeApi, private val reactionDao: ReactionDao) :
    BaseApiService<ChannelInfo> {

    override suspend fun loadData(offset: Int, limit: Int): List<ChannelInfo> {
        val response = tryIO2 {
            toffeeApi.getContents(
                "VOD",
                0, 0,
                offset, 30,
                preference.getDBVersionByApiName("getContentsV5"),
                ContentRequest(
                    0,
                    0,
                    "VOD",
                    preference.customerId,
                    preference.password,
                    offset = offset,
                    limit = limit
                )
            )
        }

        if (response.response.channels != null) {
            return response.response.channels.map {
                it.formatted_view_count = getFormattedViewsText(it.view_count)
                it.formattedDuration = discardZeroFromDuration(it.duration)
                
                val reaction = reactionDao.getReactionByContentId(it.id)
                it.subCategoryId = reaction?.reaction ?: 0
                it.reaction = getReactionIcon(reaction?.reaction)
                
                it
            }
        }
        return listOf()
    }

    private fun getReactionIcon(reaction: Int?): Int? {
        return when (reaction) {
            Like.value -> R.drawable.ic_reaction_like
            Love.value -> R.drawable.ic_reaction_love
            HaHa.value -> R.drawable.ic_reaction_haha
            Wow.value -> R.drawable.ic_reaction_wow
            Sad.value -> R.drawable.ic_reaction_sad
            Angry.value -> R.drawable.ic_reaction_angry
            else -> R.drawable.ic_like_emo
        }
    }
}