package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.database.dao.ReactionDao
import com.banglalink.toffee.data.network.request.ChannelRequestParams
import com.banglalink.toffee.data.network.request.ContentRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.repository.ContentViewPorgressRepsitory
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.enums.Reaction
import com.banglalink.toffee.model.ChannelInfo
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject

class GetContents @AssistedInject constructor(
    private val preference: Preference,
    private val toffeeApi: ToffeeApi,
    private val reactionDao: ReactionDao,
    private val viewProgressRepo: ContentViewPorgressRepsitory,
    @Assisted private val requestParams: ChannelRequestParams
    ) {

    var mOffset: Int = 0
        private set
    private val limit = 30

    suspend fun execute(): List<ChannelInfo> {

        val response = tryIO2 {
            toffeeApi.getContents(
                requestParams.type,
                requestParams.categoryId,
                requestParams.subcategoryId,
                mOffset,
                limit,
                preference.getDBVersionByApiName("getContentsV5"),
                ContentRequest(
                    requestParams.categoryId,
                    requestParams.subcategoryId,
                    requestParams.type,
                    preference.customerId,
                    preference.password,
                    offset = mOffset,
                    limit = limit
                )
            )
        }

        mOffset += response.response.count
        if (response.response.channels != null) {
            return response.response.channels.map {
                it.category = requestParams.category
                it.categoryId = requestParams.categoryId
                it.subCategoryId = requestParams.subcategoryId
                it.subCategory = requestParams.subcategory
                val reactionInfo = reactionDao.getReactionByContentId(preference.customerId, it.id)
                it.myReaction = reactionInfo?.reaction ?: Reaction.None.value
                it.viewProgress = viewProgressRepo.getProgressByContent(it.id.toLong())?.progress ?: 0L
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