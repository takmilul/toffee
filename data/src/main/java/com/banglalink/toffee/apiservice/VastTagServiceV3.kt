package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.VastTagRequestV3
import com.banglalink.toffee.data.network.response.VastTagResponseV3
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.SessionPreference
import javax.inject.Inject

class VastTagServiceV3  @Inject constructor(private val pref: SessionPreference, private val api: ToffeeApi) {
    suspend fun execute(): VastTagResponseV3 {
        return tryIO2 {
            api.getVastTagListsV3(
                pref.getDBVersionByApiName(ApiNames.GET_VAST_TAG_LIST_V3), VastTagRequestV3(
                    pref.customerId,
                    pref.password
                )
            )
        }
    }
}