package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.VastTagRequestV2
import com.banglalink.toffee.data.network.response.VastTagResponseV2
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.SessionPreference
import javax.inject.Inject

class VastTagServiceV2  @Inject constructor(private val pref: SessionPreference, private val api: ToffeeApi) {
    suspend fun execute(): VastTagResponseV2 {
        return tryIO2 {
            api.getVastTagListsV2(
                pref.getDBVersionByApiName(ApiNames.GET_VAST_TAG_LIST_V2), VastTagRequestV2(
                    pref.customerId,
                    pref.password
                )
            )
        }
    }
}