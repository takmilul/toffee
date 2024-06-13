package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.ContentCategoryRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.Category
import javax.inject.Inject

class GetContentCategoriesService @Inject constructor(
    private val preference: SessionPreference,
    private val toffeeApi: ToffeeApi
): BaseApiService<Category> {

    override suspend fun loadData(offset: Int, limit: Int): List<Category> {
        
        val response = tryIO {
            toffeeApi.getUgcContentCategoryList(
                preference.getDBVersionByApiName(ApiNames.GET_ACTIVE_INACTIVE_CATEGORIES),
                ContentCategoryRequest()
            )
        }

        if (response.response?.categories != null) {
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