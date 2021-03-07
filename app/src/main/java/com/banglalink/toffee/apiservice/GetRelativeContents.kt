package com.banglalink.toffee.apiservice

import com.banglalink.toffee.common.paging.BaseApiService
import com.banglalink.toffee.data.database.LocalSync
import com.banglalink.toffee.data.network.request.RelativeContentRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.ChannelInfo
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

data class CatchupParams(
    val id: String,
    val tags: String?,
)

class GetRelativeContents @AssistedInject constructor(
    private val preference: Preference,
    private val toffeeApi: ToffeeApi,
    private val localSync: LocalSync,
    @Assisted private val catchupParams: CatchupParams,
) : BaseApiService<ChannelInfo> {
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
                localSync.syncData(it)
                it
            }
        } else emptyList()
    }


    @dagger.assisted.AssistedFactory
    interface AssistedFactory {
        fun create(catchupParams: CatchupParams): GetRelativeContents
    }
}