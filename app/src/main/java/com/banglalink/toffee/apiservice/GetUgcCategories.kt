package com.banglalink.toffee.apiservice

import com.banglalink.toffee.common.paging.BaseApiService
import com.banglalink.toffee.data.network.request.UgcCategoryRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.UgcCategory
import javax.inject.Inject

class GetUgcCategories @Inject constructor(
    private val preference: Preference,
    private val toffeeApi: ToffeeApi
): BaseApiService<UgcCategory> {

    override suspend fun loadData(offset: Int, limit: Int): List<UgcCategory> {
        if(offset > 0) return emptyList()
        val response = tryIO2 {
            toffeeApi.getUgcCategoryList(
                preference.getDBVersionByApiName("getUgcCategories"),
                UgcCategoryRequest()
            )
        }

        if (response.response.categories != null) {
            return response.response.categories.map { cat->
                cat.subcategories.map {sub->
                    sub.categoryId = cat.id
                }
                cat
            }
        }
        return emptyList()
    }
}