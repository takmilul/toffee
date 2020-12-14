package com.banglalink.toffee.apiservice

import com.banglalink.toffee.common.paging.BaseApiService
import com.banglalink.toffee.data.network.request.RelativeContentRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
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

        return response.response.channels?: emptyList()
    }


    @AssistedInject.Factory
    interface AssistedFactory {
        fun create(catchupParams: CatchupParams): GetRelativeContents
    }
}