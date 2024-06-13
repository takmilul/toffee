package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.database.LocalSync
import com.banglalink.toffee.data.network.request.MoviesPreviewRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.util.Utils
import javax.inject.Inject

class MoviesPreviewService @Inject constructor(
    private val preference: SessionPreference,
    private val toffeeApi: ToffeeApi,
    private val localSync: LocalSync,
) {
    suspend fun loadData(
        type: String,
        categoryId: Int,
        subCategoryId: Int,
        limit: Int = 0,
        offset: Int = 0
    ): List<ChannelInfo> {

        val request = MoviesPreviewRequest(
            preference.customerId,
            preference.password
        )

        val response = tryIO {
            toffeeApi.getMoviePreviews(
                type,
                categoryId,
                subCategoryId,
                limit,
                offset,
                preference.getDBVersionByApiName(ApiNames.GET_MOVIE_PREVIEW),
                request
            )
        }

        return if (response.response?.channels != null) {
            response.response.channels.filter {
                it.isExpired = try {
                    Utils.getDate(it.contentExpiryTime).before(preference.getSystemTime())
                } catch (e: Exception) {
                    false
                }
                localSync.syncData(it, isFromCache = response.isFromCache)
                !it.isExpired
            }
        } else emptyList()
    }
}