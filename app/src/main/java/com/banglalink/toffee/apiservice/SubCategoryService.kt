package com.banglalink.toffee.apiservice

/*
class SubCategoryService @AssistedInject constructor(private val preference: Preference, private val toffeeApi: ToffeeApi, @Assisted private val categoryId: Int): BaseApiService<SubCategory> {
    
    override suspend fun loadData(offset: Int, limit: Int): List<SubCategory> {
        val response = tryIO2 {
            toffeeApi.getSubCategory(
                categoryId,
                preference.getDBVersionByApiName("getUgcSubCategories"),
                SubCategoryRequest(preference.customerId, preference.password)
            )
        }

        return response.response
    }
}*/
