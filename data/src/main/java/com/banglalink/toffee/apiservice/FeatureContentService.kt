package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.database.LocalSync
import com.banglalink.toffee.data.network.request.FeatureContentRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.enums.PageType
import com.banglalink.toffee.model.FeatureContentBean
import com.banglalink.toffee.util.Utils
import javax.inject.Inject

class FeatureContentService @Inject constructor(
    private val preference: SessionPreference,
    private val toffeeApi: ToffeeApi,
    private val localSync: LocalSync,
) {
    suspend fun loadData(type: String, pageType: PageType, categoryId: Int): FeatureContentBean {
        
        val request =  FeatureContentRequest(
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
        return response.response.apply {
            channels?.filter {
                try {
                    Utils.getDate(it.contentExpiryTime).after(preference.getSystemTime())
                } catch (e: Exception) {
                    true
                }
            }?.map {
                localSync.syncData(it)
                it
            }
        }
    }
}