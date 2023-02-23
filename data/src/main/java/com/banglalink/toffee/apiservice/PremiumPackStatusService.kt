package com.banglalink.toffee.apiservice

import android.util.Log
import com.banglalink.toffee.data.network.request.MovieCategoryDetailRequest
import com.banglalink.toffee.data.network.request.PremiumPackStatusRequest
import com.banglalink.toffee.data.network.response.PremiumPackStatusResponse
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.ActivePack
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.usecase.TAG

import javax.inject.Inject

class PremiumPackStatusService @Inject constructor (
    private val toffeeApi: ToffeeApi,
    private val preference: SessionPreference

    ){

     suspend fun loadData(contentId: Int) : List<ActivePack> {

         val request =  PremiumPackStatusRequest(
             preference.customerId,
             preference.password
         )
         var isBlNumber=0
         if ( preference.isBanglalinkNumber=="true"){
             isBlNumber=1

         }else{
             isBlNumber=0

         }
         Log.d(TAG, "getPackStatus: "+ isBlNumber.toString())
         val response = tryIO2 {
             toffeeApi.getPremiumStatus(
                 isBlNumber,
                 contentId,
                 preference.getDBVersionByApiName(ApiNames.PREMIUM_DATA_PACK_STATUS),
                 request
             )
         }
         Log.d(TAG, "getPackStatus: "+ response)
         return response.response.loginRelatedSubsHistory

    }
}