package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.CategoryRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.Category
import javax.inject.Inject

class GetCategories @Inject constructor(
    private val preference: SessionPreference,
    private val toffeeApi: ToffeeApi
): BaseApiService<Category> {

    override suspend fun loadData(offset: Int, limit: Int): List<Category> {
        if(offset > 0) return emptyList()
        val response = tryIO2 {
            toffeeApi.getUgcCategoryList(
                preference.getDBVersionByApiName("getUgcCategories"),
                CategoryRequest()
            )
        }

        if (response.response.categories != null) {
            return response.response.categories.map { cat->
                cat.subcategories?.map {sub->
                    sub.categoryId = cat.id
                }
                cat
            }
        }
        return emptyList()
    }
}