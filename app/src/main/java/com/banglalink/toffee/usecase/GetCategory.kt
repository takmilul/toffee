package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.NavCategoryRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.NavCategoryGroup

class GetCategory(private val preference: Preference,private val toffeeApi: ToffeeApi) {

    suspend fun execute():NavCategoryGroup{
        val response = tryIO2 { toffeeApi.getCategory(preference.getDBVersionByApiName("getCategoriesV2"),NavCategoryRequest()) }
        return response.response.categories
    }
}