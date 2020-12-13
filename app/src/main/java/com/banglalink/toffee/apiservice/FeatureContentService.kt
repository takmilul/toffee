package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.UgcFeatureContentRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.enums.PageType
import com.banglalink.toffee.model.FeatureContentBean
import javax.inject.Inject

class FeatureContentService @Inject constructor(
    private val preference: Preference,
    private val toffeeApi: ToffeeApi,
) {
    suspend fun loadData(type: String, pageType: PageType, categoryId: Int): FeatureContentBean {
        
        val request =  UgcFeatureContentRequest(
            preference.customerId,
            preference.password
        )

        val response = tryIO2 {
            toffeeApi.getUgcFeatureContents(
                type,
                pageType.value,
                categoryId,
                preference.getDBVersionByApiName("getUgcFeatureCategoryContents"),
                request
            )
        }
        return response.response
    }
}