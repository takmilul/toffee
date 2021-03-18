package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.database.LocalSync
import com.banglalink.toffee.data.network.request.MoviesPreviewRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.ChannelInfo
import javax.inject.Inject

class MoviesPreviewService @Inject constructor(
    private val preference: SessionPreference,
    private val toffeeApi: ToffeeApi,
    private val localSync: LocalSync,
) {
    suspend fun loadData(type: String, categoryId: Int, subCategoryId: Int, limit: Int = 0, offset: Int = 0): List<ChannelInfo> {

        val request = MoviesPreviewRequest(
            preference.customerId,
            preference.password
        )

        val response = tryIO2 {
            toffeeApi.getMoviePreviews(
                type,
                categoryId,
                subCategoryId,
                limit,
                offset,
                preference.getDBVersionByApiName("getUgcMoviePreview"),
                request
            )
        }

        return if (response.response.channels != null) {
            response.response.channels.map {
                localSync.syncData(it)
                it
            }
        } else emptyList()
    }
}