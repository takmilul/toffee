package com.banglalink.toffee.apiservice

import com.banglalink.toffee.common.paging.BaseApiService
import com.banglalink.toffee.data.database.dao.ReactionDao
import com.banglalink.toffee.data.network.request.MyChannelPlaylistVideosRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.repository.ContentViewPorgressRepsitory
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.enums.Reaction
import com.banglalink.toffee.model.ChannelInfo
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject

data class MyChannelPlaylistContentParam(val channelOwnerId: Int, val isOwner: Int, val playlistId: Int)

class MyChannelPlaylistVideosService @AssistedInject constructor(
    private val preference: Preference,
    private val toffeeApi: ToffeeApi,
    private val reactionDao: ReactionDao,
    private val viewProgressRepo: ContentViewPorgressRepsitory,
    @Assisted private val requestParams: MyChannelPlaylistContentParam):
    BaseApiService<ChannelInfo> {
    
    override suspend fun loadData(offset: Int, limit: Int): List<ChannelInfo> {
        val response = tryIO2 {
            toffeeApi.getMyChannelPlaylistVideos(
                requestParams.channelOwnerId,
                requestParams.isOwner, 
                requestParams.playlistId,
                limit, offset,
                preference.getDBVersionByApiName("getUgcContentByPlaylist"),
                MyChannelPlaylistVideosRequest(preference.customerId, preference.password)
            )
        }

        return if (response.response.channels != null) {
            response.response.channels.map {
                it.viewProgress = viewProgressRepo.getProgressByContent(it.id.toLong())?.progress ?: 0L
                val reactionInfo = reactionDao.getReactionByContentId(preference.customerId, it.id.toLong())
                it.myReaction = reactionInfo?.reactionType ?: Reaction.None.value
                it
            }
        } else emptyList()
    }

    @AssistedInject.Factory
    interface AssistedFactory {
        fun create(requestParams: MyChannelPlaylistContentParam): MyChannelPlaylistVideosService
    }
}