package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.MovieCategoryDetailRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.MovieCategoryDetailBean
import javax.inject.Inject

class MovieCategoryDetailService @Inject constructor(
    private val preference: SessionPreference,
    private val toffeeApi: ToffeeApi,
) {
    suspend fun loadData(type: String, categoryId: Int, limit: Int = 0, offset: Int = 0): MovieCategoryDetailBean {

        val request =  MovieCategoryDetailRequest(
            preference.customerId,
            preference.password
        )

        val response = tryIO {
            toffeeApi.getMovieCategoryDetail(
                type,
                categoryId,
                limit,
                offset,
                preference.getDBVersionByApiName(ApiNames.GET_MOVIE_CATEGORY_DETAILS),
                request
            )
        }
        return response.response
    }
}