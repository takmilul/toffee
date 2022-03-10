package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.VastTagRequest
import com.banglalink.toffee.data.network.response.VastTagResponse
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.SessionPreference
import javax.inject.Inject

class VastTagService  @Inject constructor(private val pref: SessionPreference, private val api: ToffeeApi) {
    suspend fun execute(): VastTagResponse {
        return tryIO2 {
            api.getVastTagLists(
                pref.getDBVersionByApiName(ApiNames.GET_VAST_TAG_LIST), VastTagRequest(
                    pref.customerId,
                    pref.password
                )
            )
        }
    }
}