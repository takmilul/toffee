package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.database.LocalSync
import com.banglalink.toffee.data.network.request.RelativeContentRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.util.Utils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

data class CatchupParams(
    val id: String,
    val tags: String?,
    val categoryId: Int = 0,
    val subCategoryId: Int = 0
)

class GetRelativeContents @AssistedInject constructor(
    private val preference: SessionPreference,
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
                    catchupParams.categoryId,
                    catchupParams.subCategoryId,
                    offset,
                    limit
                )
            )
        }

        return if (response.response.channels != null) {
            response.response.channels.filter {
                try {
                    Utils.getDate(it.contentExpiryTime).after(preference.getSystemTime())
                } catch (e: Exception) {
                    true
                }
            }.map {
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