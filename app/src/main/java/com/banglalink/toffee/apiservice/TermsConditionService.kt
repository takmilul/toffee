package com.banglalink.toffee.apiservice

import android.util.Log
import com.banglalink.toffee.data.network.request.MyChannelEditRequest
import com.banglalink.toffee.data.network.request.TermsConditionRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.MyChannelEditBean
import com.banglalink.toffee.model.TermsAndCondition
import com.google.gson.Gson
import javax.inject.Inject

class TermsConditionService @Inject constructor( private val preference: Preference,private val toffeeApi: ToffeeApi) {

    suspend fun execute(termsConditionRequest:TermsConditionRequest): TermsAndCondition {
//        val response = tryIO2 {
//            val termsConditionRequest:TermsConditionRequest(0,"10")
//            toffeeApi.getVideoTermsAndCondition(0,termsConditionRequest)
//        }
//        Log.e("api TAG", Gson().toJson(response))
//        Log.e("api TAG", Gson().toJson(termsConditionRequest))
        val response = tryIO2 {
            toffeeApi.getVideoTermsAndCondition(0,
                termsConditionRequest.apply {
                    customerId = preference.customerId
                    password = preference.password
                }
            )
        }
        return response.response
    }
}