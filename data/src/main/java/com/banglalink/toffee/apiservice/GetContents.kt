package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.ChannelRequestParams
import com.banglalink.toffee.data.network.request.ContentRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.util.Utils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

class GetContents @AssistedInject constructor(
    private val preference: SessionPreference,
    private val toffeeApi: ToffeeApi,
    @Assisted private val requestParams: ChannelRequestParams,
) : BaseApiService<ChannelInfo> {

    override suspend fun loadData(offset: Int, limit: Int): List<ChannelInfo> {
        val response = tryIO2 {
            toffeeApi.getContents(
                requestParams.type,
                requestParams.categoryId,
                requestParams.subcategoryId,
                offset,
                limit,
                preference.getDBVersionByApiName(ApiNames.GET_CONTENTS_V5),
                ContentRequest(
                    preference.customerId,
                    preference.password,
                )
            )
        }

        if (response.response.channels != null) {
            return response.response.channels.map {
                it.isExpired = try {
                    Utils.getDate(it.contentExpiryTime).before(preference.getSystemTime())
                } catch (e: Exception) {
                    false
                }
                it.category = requestParams.category
                it.categoryId = requestParams.categoryId
                it.subCategoryId = requestParams.subcategoryId
                it.subCategory = requestParams.subcategory
                it
            }
        }
        return emptyList()
    }

    @dagger.assisted.AssistedFactory
    interface AssistedFactory {
        fun create(requestParams: ChannelRequestParams): GetContents
    }
}