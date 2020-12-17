package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.MoviesComingSoonRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.ComingSoonContent
import javax.inject.Inject

class MoviesComingSoonService @Inject constructor(
    private val preference: Preference,
    private val toffeeApi: ToffeeApi,
) {
    suspend fun loadData(type: String, categoryId: Int, subCategoryId: Int, limit: Int = 0, offset: Int = 0): List<ComingSoonContent> {

        val request = MoviesComingSoonRequest(
            preference.customerId,
            preference.password
        )

        val response = tryIO2 {
            toffeeApi.getComingSoonPosters(
                type,
                categoryId,
                subCategoryId,
                limit,
                offset,
                preference.getDBVersionByApiName("getUgcComingSoon"),
                request
            )
        }

        return response.response.channels ?: emptyList()
    }
}