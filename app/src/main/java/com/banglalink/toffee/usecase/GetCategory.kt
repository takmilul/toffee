package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.NavCategoryRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.model.NavCategoryGroup

class GetCategory(private val toffeeApi: ToffeeApi) {

    suspend fun execute():NavCategoryGroup{
        val response = tryIO { toffeeApi.getCategory(NavCategoryRequest()) }
        return response.response.categories
    }
}