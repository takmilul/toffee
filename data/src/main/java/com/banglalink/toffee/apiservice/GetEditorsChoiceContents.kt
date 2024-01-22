package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.database.LocalSync
import com.banglalink.toffee.data.network.request.AllUserChannelsEditorsChoiceRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.EditorsChoiceFeaturedRequestParams
import com.banglalink.toffee.util.Utils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.serialization.Serializable

@Serializable
data class ApiCategoryRequestParams(
    val type: String,
    val isCategory: Int,
    val categoryId: Int,
)

class GetEditorsChoiceContents @AssistedInject constructor(
    private val preference: SessionPreference,
    private val toffeeApi: ToffeeApi,
    private val localSync: LocalSync,
    @Assisted private val requestParams: EditorsChoiceFeaturedRequestParams,
) : BaseApiService<ChannelInfo> {

    override suspend fun loadData(offset: Int, limit: Int): List<ChannelInfo> {
        
        val request = AllUserChannelsEditorsChoiceRequest(
            preference.customerId,
            preference.password
        )

        val response = tryIO {
            toffeeApi.getUgcEditorsChoice(
                requestParams.type,
                requestParams.pageType.value,
                requestParams.categoryId,
                preference.getDBVersionByApiName(ApiNames.GET_EDITOR_CHOICE),
                request
            )
        }
        return if (response.response.channels != null) {
            response.response.channels.filter {
                it.isExpired = try {
                    Utils.getDate(it.contentExpiryTime).before(preference.getSystemTime())
                } catch (e: Exception) {
                    false
                }
                if (!it.isExpired) {
                    it.categoryId = requestParams.categoryId
                    localSync.syncData(it, isFromCache = response.isFromCache)
                }
                !it.isExpired
            }
        } else emptyList()
    }

    @dagger.assisted.AssistedFactory
    interface AssistedFactory {
        fun create(requestParams: EditorsChoiceFeaturedRequestParams): GetEditorsChoiceContents
    }
}