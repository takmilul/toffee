package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.MovieCategoryDetailRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.MovieCategoryDetailBean
import javax.inject.Inject

class MovieCategoryDetailService @Inject constructor(
    private val preference: Preference,
    private val toffeeApi: ToffeeApi,
) {
    suspend fun loadData(type: String, limit: Int = 0, offset: Int = 0): MovieCategoryDetailBean {

        val request =  MovieCategoryDetailRequest(
            preference.customerId,
            preference.password
        )

        val response = tryIO2 {
            toffeeApi.getMovieCategoryDetail(
                type,
                limit,
                offset,
                preference.getDBVersionByApiName("getUgcMovieCategoryDetails"),
                request
            )
        }
        return response.response
    }
}