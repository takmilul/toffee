package com.banglalink.toffee.apiservice

import com.banglalink.toffee.common.paging.BaseApiService
import com.banglalink.toffee.data.database.LocalSync
import com.banglalink.toffee.data.database.dao.ReactionDao
import com.banglalink.toffee.data.database.dao.ViewCountDAO
import com.banglalink.toffee.data.network.request.RelativeContentRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.repository.ContentViewPorgressRepsitory
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.enums.Reaction
import com.banglalink.toffee.model.ChannelInfo
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject

data class CatchupParams(
    val id: String,
    val tags: String?
)

class GetRelativeContents @AssistedInject constructor(
        private val preference: Preference,
        private val toffeeApi: ToffeeApi,
        private val localSync: LocalSync,
//        private val viewCountDAO: ViewCountDAO,
//        private val reactionDao: ReactionDao,
//        private val viewProgressRepo: ContentViewPorgressRepsitory,
        @Assisted private val catchupParams: CatchupParams
): BaseApiService<ChannelInfo>{
    override suspend fun loadData(offset: Int, limit: Int): List<ChannelInfo> {
        val response = tryIO2 {
            toffeeApi.getRelativeContents(
                RelativeContentRequest(
                    catchupParams.id,
                    catchupParams.tags ?: "",
                    preference.customerId,
                    preference.password,
                    offset = offset,
                    limit = limit
                )
            )
        }

        return if (response.response.channels != null) {
            response.response.channels.map {
//                val viewCount = viewCountDAO.getViewCountByChannelId(it.id.toInt())
//                if(viewCount!=null){
//                    it.view_count= viewCount.toString()
//                }
//                it.viewProgress = viewProgressRepo.getProgressByContent(it.id.toLong())?.progress ?: 0L
//                val reactionInfo = reactionDao.getReactionByContentId(preference.customerId, it.id.toLong())
//                it.myReaction = reactionInfo?.reactionType ?: Reaction.None.value
                localSync.syncData(it)
                it
            }
        } else emptyList()
    }


    @AssistedInject.Factory
    interface AssistedFactory {
        fun create(catchupParams: CatchupParams): GetRelativeContents
    }
}