package com.banglalink.toffee.apiservice

import com.banglalink.toffee.common.paging.BaseApiService
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.FeaturedPartner
import com.banglalink.toffee.model.FeaturedPartnerRequest
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

class FeaturedPartnerService @AssistedInject constructor(
    private val preference: SessionPreference,
    private val toffeeApi: ToffeeApi,
    @Assisted private val type: String,
) : BaseApiService<FeaturedPartner> {
    
    override suspend fun loadData(offset: Int, limit: Int): List<FeaturedPartner> {
        val response = tryIO2 {
            toffeeApi.getFeaturedPartners(
                type,
                limit,
                offset,
                preference.getDBVersionByApiName("getUgcFeaturePartnerList"),
                FeaturedPartnerRequest(
                    preference.customerId,
                    preference.password,
                )
            )
        }
        return response.response.featuredPartners ?: emptyList()
    }
    
    @dagger.assisted.AssistedFactory
    interface AssistedFactory {
        fun create(type: String): FeaturedPartnerService
    }
}