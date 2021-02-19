package com.banglalink.toffee.apiservice

import com.banglalink.toffee.common.paging.BaseApiService
import com.banglalink.toffee.data.database.LocalSync
import com.banglalink.toffee.data.database.dao.ReactionDao
import com.banglalink.toffee.data.database.dao.ViewCountDAO
import com.banglalink.toffee.data.network.request.MyChannelVideosRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.repository.ContentViewPorgressRepsitory
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.enums.Reaction
import com.banglalink.toffee.model.ChannelInfo
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject

data class MyChannelVideosRequestParams(
    val type: String,
    val isOwner: Int,
    val channelOwnerId: Int,
    val categoryId: Int,
    val subcategoryId: Int,
    val isPublic: Int
)

class MyChannelVideosService @AssistedInject constructor(
        private val preference: Preference,
        private val toffeeApi: ToffeeApi,
        private val localSync: LocalSync,
//        private val reactionDao: ReactionDao,
//        private val viewCountDAO: ViewCountDAO,
//        private val viewProgressRepo: ContentViewPorgressRepsitory,
        @Assisted private val requestParams: MyChannelVideosRequestParams
) :
    BaseApiService<ChannelInfo> {

    override suspend fun loadData(offset: Int, limit: Int): List<ChannelInfo> {
        val response = tryIO2 {
            toffeeApi.getMyChannelVideos(
                requestParams.type,
                requestParams.isOwner, requestParams.channelOwnerId,
                requestParams.categoryId,
                requestParams.subcategoryId,
                requestParams.isPublic,
                limit, offset,
                preference.getDBVersionByApiName("getUgcChannelAllContent"),
                MyChannelVideosRequest(preference.customerId, preference.password, offset, limit)
            )
        }

        if (response.response.channels != null) {
            return response.response.channels.map {
//                val viewCount = viewCountDAO.getViewCountByChannelId(it.id.toInt())
//                if(viewCount!=null){
//                    it.view_count= viewCount.toString()
//                }
//                val reactionInfo = reactionDao.getReactionByContentId(preference.customerId, it.id.toLong())
//                it.myReaction = reactionInfo?.reactionType ?: Reaction.None.value
//                it.viewProgress = viewProgressRepo.getProgressByContent(it.id.toLong())?.progress ?: 0L
                localSync.syncData(it)
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